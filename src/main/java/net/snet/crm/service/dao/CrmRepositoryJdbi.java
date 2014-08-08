package net.snet.crm.service.dao;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.mixins.CloseMe;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public class CrmRepositoryJdbi implements CrmRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(CrmRepositoryJdbi.class);

	private final static Map<String, Long> COUNTRIES = ImmutableMap.of("CZ", 10L, "PL", 20L);
	private final static Map<String, String> CONNECTION_FIELDS;
	static {
		CONNECTION_FIELDS = Maps.newHashMap();
		CONNECTION_FIELDS.put("auth_type", "auth_type");
		CONNECTION_FIELDS.put("auth_name", "auth_name");
		CONNECTION_FIELDS.put("auth_value", "auth_value");
		CONNECTION_FIELDS.put("downlink", "downlink");
		CONNECTION_FIELDS.put("uplink", "uplink");
		CONNECTION_FIELDS.put("is_public_ip", "is_public_ip");
		CONNECTION_FIELDS.put("ip", "ip");
	};

	private final CrmDatabase db;

	public CrmRepositoryJdbi(DBI dbi) {
		db = dbi.onDemand(CrmDatabase.class);
	}

	@Override
	public Map<String, Object> insertCustomer(Map<String, Object> customer) {
		db.begin();
		try {
			long id = db.lastCustomerId() + 1;
			long auditId = db.lastAuditId() + 1;
			String name = customer.get("name").toString();
			db.insertCustomer(id, auditId, name, now());
			long auditItemId = db.lastAuditItemId() + 1;
			db.insertAudit(auditItemId, auditId, 41, 2, now(), "Customer.fName", "", name);
			db.commit();
			return findCustomerById(id);
		} catch (Exception e) {
			db.rollback();
			throw new RuntimeException(e);
		}
	}

	private Timestamp now() {
		return new Timestamp(new DateTime().getMillis());
	}

	@Override
	public Map<String, Object> findCustomerById(final long customerId) {
		return db.withHandle(new HandleCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> withHandle(Handle handle) throws Exception {
				return handle.createQuery("SELECT * FROM customers WHERE id=:id")
						.bind("id", customerId)
						.first();
			}
		});
	}

	@Override
	public Map<String, Object> insertAgreement(long customerId, String country) {
		checkArgument(COUNTRIES.keySet().contains(country), "unknown country '%s'", country);
		Map<String, Object> customer = findCustomerById(customerId);
		checkNotNull(customer, "customer with id '%s' does not exist", customerId);
		db.begin();
		try {
			long lastAgreementId = db.lastAgreementIdByCountry(country);
			if (lastAgreementId == 0) {
				lastAgreementId = COUNTRIES.get(country) * 100000;
			}
			long agreementId = lastAgreementId + 1;
			checkState(agreementId > COUNTRIES.get(country) * 100000, "inconsistent agreement id '%s', check agreements table consistency", agreementId);
			db.insertAgreement(agreementId, country, customerId);
			long contractNumber = agreementId % 1000000;
			String agreements = "" + contractNumber;
			Optional<Object> currentAgreements = Optional.fromNullable(customer.get("contract_no"));
			if (currentAgreements.isPresent() && currentAgreements.get().toString().trim().length() > 0) {
				agreements = currentAgreements.get().toString().trim() + ", " + contractNumber;
			}
			db.setCustomerAgreements(customerId, agreements);
			db.commit();
			return findAgreementById(agreementId);
		} catch (Exception e) {
			db.rollback();
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, Object> findAgreementById(final long agreementId) {
		return db.withHandle(new HandleCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> withHandle(Handle handle) throws Exception {
				return handle.createQuery("SELECT * FROM agreements WHERE id=:id")
						.bind("id", agreementId)
						.first();
			}
		});
	}

	@Override
	public Map<String, Object> insertService(long agreementId) {
		Map<String, Object> agreement = findAgreementById(agreementId);
		checkNotNull(agreement, "agreement with id '%s' does not exist", agreementId);
		checkNotNull(agreement.get("customer_id"), "agreement with id '%s' is not associated with a customer", agreementId);
		db.begin();
		try {
			long lastServiceId = lastServiceIdByAgreement(agreementId);
			checkState((lastServiceId % 100) < 99, "cannot add new service to the agreement '%s', max of 99 services already exists", agreementId);
			long serviceId = lastServiceId + 1;
			db.insertService(serviceId, Long.valueOf(agreement.get("customer_id").toString()), now());
			db.insertServiceInfo(serviceId);
			db.commit();
			return findServiceById(serviceId);
		} catch (Exception e) {
			db.rollback();
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, Object> findServiceById(final long serviceId) {
		final Map<String, Object> service = db.withHandle(new HandleCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> withHandle(Handle handle) throws Exception {
				return handle.createQuery("SELECT * FROM services WHERE id=:id")
						.bind("id", serviceId)
						.first();
			}
		});
		final Map<String, Object> serviceInfo = db.withHandle(new HandleCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> withHandle(Handle handle) throws Exception {
				return handle.createQuery("SELECT * FROM services_info WHERE service_id=:service_id")
						.bind("service_id", serviceId)
						.first();
			}
		});
		service.put("status", serviceInfo.get("status"));
		return service;
	}

	private long lastServiceIdByAgreement(long agreementId) {
		final long first = agreementId * 100;
		final long last = (agreementId + 1) * 100;
		long lastUsed = db.lastServiceIdInRange(first, last);
		if (lastUsed == 0) {
			lastUsed = first;
		}
		return lastUsed;
	}

	@Override
	public Map<String, Object> insertConnection(long serviceId) {
		Map<String, Object> service = findServiceById(serviceId);
		checkNotNull(service.get("id"), "service with id '%s' does not exist", serviceId);
		Map<String, Object> existingConnection = findConnectionByServiceId(serviceId);
		checkState(existingConnection == null, "connection for service '%s' already exist", serviceId);
		db.begin();
		try {
			db.insertConnection(serviceId);
			db.commit();
			return findConnectionByServiceId(serviceId);
		} catch (Exception e) {
			db.rollback();
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, Object> findConnectionByServiceId(final long serviceId) {
		return db.withHandle(new HandleCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> withHandle(Handle handle) throws Exception {
				return handle.createQuery("SELECT * FROM connections WHERE service_id=:service_id")
						.bind("service_id", serviceId)
						.first();
			}
		});
	}

	@Override
	public Map<String, Object> updateConnection(final long serviceId, Iterable<Map.Entry<String, Object>> rawUpdates) {
		final Map<String, Object> connection = findConnectionByServiceId(serviceId);
		checkNotNull(connection, "connection for service '%s' does not exist", serviceId);
		Iterable<Map.Entry<String, Object>> updates = Iterables.filter(rawUpdates, new Predicate<Map.Entry<String, Object>>() {
			@Override
			public boolean apply(@Nullable Map.Entry<String, Object> update) {
				return CONNECTION_FIELDS.containsKey(update.getKey())
						&& !update.getValue().equals(connection.get(update.getKey()));
			}
		});
		final HashMap<String, Object> updateMap = Maps.newHashMap();
		List<String> fields = Lists.newArrayList();
		for (Map.Entry<String, Object> update : updates) {
			updateMap.put(update.getKey(), update.getValue());
			fields.add(update.getKey() + "=:" + update.getKey());
		}
		if (updateMap.size() == 0) {
			LOGGER.debug("nothing to update returning original connection for service '{}'", serviceId);
			return findConnectionByServiceId(serviceId);
		}
		final String sqlTemplate = "UPDATE connections SET " + Joiner.on(", ").join(fields) + " WHERE service_id=:service_id";
		db.begin();
		try {
			Integer updated = db.withHandle(new HandleCallback<Integer>() {
				@Override
				public Integer withHandle(Handle handle) throws Exception {
					return handle.createStatement(sqlTemplate)
							.bind("service_id", serviceId)
							.bindFromMap(updateMap)
							.execute();
				}
			});
			if (updated != 1) {
				throw new IllegalStateException("failed to update connection for service '" + serviceId + "'");
			}
			db.commit();
			return findConnectionByServiceId(serviceId);
		} catch (Exception e) {
			db.rollback();
			throw new RuntimeException(e);
		}
	}

	public interface CrmDatabase extends Transactional<CrmDatabase>, GetHandle, CloseMe {

		@SqlQuery("SELECT max(id) FROM customers")
		long lastCustomerId();

		@SqlQuery("SELECT max(history_id) FROM audit_items")
		long lastAuditId();

		@SqlQuery("SELECT max(id) FROM audit_items")
		long lastAuditItemId();

		@SqlQuery("SELECT max(id) FROM agreements WHERE country=:country")
		long lastAgreementIdByCountry(@Bind("country") String country);

		@SqlQuery("SELECT max(id) FROM services WHERE :first < id AND id < :last ")
		long lastServiceIdInRange(@Bind("first") long fist, @Bind("last") long last);

		@SqlUpdate("INSERT INTO customers (id, history_id, name, public_id, inserted_on, is_active) " +
				"VALUES (:id, :history_id, :name, '9999999', :inserted_on, false)")
		void insertCustomer(@Bind("id") long id, @Bind("history_id") long auditId,
		                                    @Bind("name") String name, @Bind("inserted_on") Timestamp insertedOn);

		@SqlUpdate("INSERT INTO audit_items (id, history_id, history_type_label_id, user_id, time_stamp, field_name, old_value, new_value) " +
				"VALUES (:id, :history_id, :history_type_label_id, :user_id, :time_stamp, :field_name, :old_value, :new_value)")
		void insertAudit(@Bind("id") long id, @Bind("history_id") long auditId,
                     @Bind("history_type_label_id") long auditType, @Bind("user_id") long userId,
                     @Bind("time_stamp") Timestamp stamp, @Bind("field_name") String field,
                     @Bind("old_value") String oldValue, @Bind("new_value") String newValue);

		@SqlUpdate("INSERT INTO agreements (id, country, customer_id) " +
				"VALUES (:id, :country, :customer_id)")
		void insertAgreement(@Bind("id") long id, @Bind("country") String country,
		                                     @Bind("customer_id") long customerId);

		@SqlUpdate("UPDATE customers SET contract_no=:agreements WHERE id=:id")
		void setCustomerAgreements(@Bind("id") long customerId, @Bind("agreements") String agreements);

		@SqlUpdate("INSERT INTO services (id, customer_id, period_from, name, price) " +
				"VALUES (:service_id, :customer_id, :period_from, 'NEW', 0)")
		void insertService(@Bind("service_id") long serviceId, @Bind("customer_id") long customerId,
		                   @Bind("period_from") Timestamp periodFrom);

		@SqlUpdate("INSERT INTO services_info (service_id, status, other_info) " +
				"VALUES (:service_id, 'NEW', '{}')")
		void insertServiceInfo(@Bind("service_id") long serviceId);

		@SqlUpdate("INSERT INTO connections (service_id) VALUES (:service_id)")
		void insertConnection(@Bind("service_id") long serviceId);
	}
}
