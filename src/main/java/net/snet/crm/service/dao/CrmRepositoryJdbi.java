package net.snet.crm.service.dao;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
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

import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

public class CrmRepositoryJdbi implements CrmRepository {

	private final static Set<String> COUNTRIES = ImmutableSet.of("CZ", "PL");

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
	public Map<String, Object> findCustomerById(final long custoemrId) {
		return db.withHandle(new HandleCallback<Map<String, Object>>() {
			@Override
			public Map<String, Object> withHandle(Handle handle) throws Exception {
				return handle.createQuery("SELECT * FROM customers WHERE id=:id")
						.bind("id", custoemrId)
						.first();
			}
		});
	}

	@Override
	public Map<String, Object> insertAgreement(long customerId, String country) {
		checkArgument(COUNTRIES.contains(country), "unknown country '%s'", country);
		Map<String, Object> customer = findCustomerById(customerId);
		checkNotNull(customer.get("id"), "customer with id '%s' does not exist", customerId);
		db.begin();
		try {
			long agreementId = db.lastAgreementIdByCountry(country) + 1;
			db.insertAgreement(agreementId, country, customerId);
			String agreements = "" + agreementId;
			Optional<Object> currentAgreements = Optional.fromNullable(customer.get("contract_no"));
			if (currentAgreements.isPresent() && currentAgreements.get().toString().trim().length() > 0) {
				agreements = currentAgreements.get().toString().trim() + ", " + agreementId;
			}
			db.setCustomerAgreements(customerId, agreements);
			db.commit();
			return findAgreementById(agreementId);
		} catch (Exception e) {
			db.rollback();
			throw new RuntimeException(e);
		}
	}

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

	public interface CrmDatabase extends Transactional<CrmDatabase>, GetHandle, CloseMe {

		@SqlQuery("SELECT max(id) FROM customers")
		public abstract long lastCustomerId();

		@SqlQuery("SELECT max(history_id) FROM audit_items")
		public abstract long lastAuditId();

		@SqlQuery("SELECT max(id) FROM audit_items")
		public abstract long lastAuditItemId();

		@SqlQuery("SELECT max(id) FROM agreements where country=:country")
		public abstract long lastAgreementIdByCountry(@Bind("country") String country);

		@SqlUpdate("INSERT INTO customers (id, history_id, name, public_id, inserted_on, is_active) " +
				"VALUES (:id, :history_id, :name, '9999999', :inserted_on, false)")
		public abstract void insertCustomer(@Bind("id") long id, @Bind("history_id") long auditId,
		                                    @Bind("name") String name, @Bind("inserted_on") Timestamp insertedOn);

		@SqlUpdate("INSERT INTO audit_items (id, history_id, history_type_label_id, user_id, time_stamp, field_name, old_value, new_value) " +
				"VALUES (:id, :history_id, :history_type_label_id, :user_id, :time_stamp, :field_name, :old_value, :new_value)")
		public abstract void insertAudit(@Bind("id") long id, @Bind("history_id") long auditId,
		                                 @Bind("history_type_label_id") long auditType, @Bind("user_id") long userId,
		                                 @Bind("time_stamp") Timestamp stamp, @Bind("field_name") String field,
		                                 @Bind("old_value") String oldValue, @Bind("new_value") String newValue);

		@SqlUpdate("INSERT INTO agreements (id, country, customer_id) " +
				"VALUES (:id, :country, :customer_id)")
		public abstract void insertAgreement(@Bind("id") long id, @Bind("country") String country,
		                                     @Bind("customer_id") long customerId);

		@SqlUpdate("UPDATE customers SET contract_no=:agreements WHERE id=:id")
		public abstract void setCustomerAgreements(@Bind("id") long customerId, @Bind("agreements") String agreements);
	}
}
