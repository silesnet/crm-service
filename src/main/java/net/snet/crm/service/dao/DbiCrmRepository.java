package net.snet.crm.service.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.snet.crm.service.utils.Databases;
import net.snet.crm.service.utils.Utils;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.mixins.CloseMe;
import org.skife.jdbi.v2.sqlobject.mixins.GetHandle;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.LongMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;
import static net.snet.crm.service.utils.Databases.updateRecordWithId;

public class DbiCrmRepository implements CrmRepository {
  private static final Logger logger = LoggerFactory.getLogger(DbiCrmRepository.class);

  private static final Map<String, Long> COUNTRIES = ImmutableMap.of("CZ", 10L, "PL", 20L);
  public static final int SERVICE_COUNTRY_MULTIPLIER = 100000;
  private final String TRANSLATE_FROM_CHARS = "ÁĄÄČĆĎÉĚĘËÍŁŇŃÓÖŘŠŚŤÚŮÜÝŽŻŹáąäčćďéěęëíłňńóöřšśťúůüýžżź.-,;:&+? ";
  private final String TRANSLATE_TO_CHARS = "aaaccdeeeeilnnoorsstuuuyzzzaaaccdeeeeilnnoorsstuuuyzzz";
  private static final String DRAFT_TABLE = "drafts2";
  private static final String PRODUCT_TABLE = "products";

  private static final Map<String, String> CONNECTION_FIELDS;

  static {
    CONNECTION_FIELDS = Maps.newHashMap();
    CONNECTION_FIELDS.put("auth_type", "auth_type");
    CONNECTION_FIELDS.put("auth_name", "auth_name");
    CONNECTION_FIELDS.put("auth_value", "auth_value");
    CONNECTION_FIELDS.put("downlink", "downlink");
    CONNECTION_FIELDS.put("uplink", "uplink");
    CONNECTION_FIELDS.put("is_public_ip", "is_public_ip");
    CONNECTION_FIELDS.put("ip", "ip");
  }

  private final CrmDatabase db;
  private final DBI dbi;

  public DbiCrmRepository(final DBI dbi) {
    this.dbi = dbi;
    this.db = dbi.onDemand(CrmDatabase.class);
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
  public void deleteCustomer(final long customerId) {
    db.withHandle(new HandleCallback<Object>() {
      @Override
      public Object withHandle(Handle handle) throws Exception {
        handle.createStatement("DELETE from customers WHERE id=:id")
            .bind("id", customerId)
            .execute();
        return null;
      }
    });
  }

  @Override
  public Map<String, Object> updateCustomer(final long customerId, final Map<String, Object> updates) {
    if (updates.size() == 0) {
      logger.debug("nothing to update returning original customer '{}'", customerId);
      return findCustomerById(customerId);
    }
    final String sql = sqlUpdate("customers", updates, "id");
    db.begin();
    try {
      Integer updated = db.withHandle(new HandleCallback<Integer>() {
        @Override
        public Integer withHandle(Handle handle) throws Exception {
          return handle.createStatement(sql)
              .bind("id", customerId)
              .bindFromMap(updates)
              .execute();
        }
      });
      if (updated != 1) {
        throw new IllegalStateException("failed to update customer '" + customerId + "'");
      }
      db.commit();
      return findCustomerById(customerId);
    } catch (Exception e) {
      db.rollback();
      throw new RuntimeException(e);
    }
  }

  private String sqlUpdate(String table, Map<String, Object> updates, String identity) {
    return "UPDATE " + table + " SET " + updateExpression(updates) +
        " WHERE " + identity + "=:" + identity;
  }

  private String updateExpression(Map<String, Object> updates) {
    final ArrayList<String> items = Lists.newArrayList();
    for (String column : updates.keySet()) {
      items.add(column + "=:" + column);
    }
    return Joiner.on(", ").join(items);
  }

  @Override
  public void setCustomerAgreements(long customerId, String agreements) {
    db.setCustomerAgreements(customerId, agreements);
  }

  @Override
  public Map<String, Object> insertAgreement(long customerId, String country) {
    checkArgument(COUNTRIES.keySet().contains(country), "unknown country '%s'", country);
    Map<String, Object> customer = findCustomerById(customerId);
    checkNotNull(customer, "customer with id '%s' does not exist", customerId);
    db.begin();
    try {
      long agreementId = db.reusableAgreementIdByCountry(country);
      if (agreementId == 0) {
        agreementId = nextAgreementId(country);
        db.insertAgreement(agreementId, country, customerId);
      } else {
        int changes = 0;
        changes += db.updateAgreementCustomer(agreementId, customerId);
        changes += db.updateAgreementStatus(agreementId, "DRAFT");
        checkState(changes == 2, "failed to reuse agreement '%d' for customer '%d'", agreementId, customer);
      }
      db.commit();
      return findAgreementById(agreementId);
    } catch (Exception e) {
      db.rollback();
      throw new RuntimeException(e);
    }
  }

  private long nextAgreementId(String country) {
    long lastAgreementId = db.lastAgreementIdByCountry(country);
    if (lastAgreementId == 0) {
      lastAgreementId = COUNTRIES.get(country) * (SERVICE_COUNTRY_MULTIPLIER / 10);
    }
    long agreementId = lastAgreementId + 1;
    checkState(agreementId > COUNTRIES.get(country) * (SERVICE_COUNTRY_MULTIPLIER / 10), "inconsistent agreement id '%s', check agreements table consistency", agreementId);
    return agreementId;
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
  public List<Map<String, Object>> findAgreementsByCustomerId(final long customerId) {
    return db.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        return handle.createQuery("SELECT * FROM agreements WHERE customer_id=:customer_id")
            .bind("customer_id", customerId)
            .list();
      }
    });
  }

  @Override
  public Map<String, Object> updateAgreementStatus(final long agreementId, final String status) {
    int rowsChanged = db.updateAgreementStatus(agreementId, status);
    checkState(rowsChanged == 1, "agreement with id '%s' does not exist or cannot be changed", agreementId);
    return findAgreementById(agreementId);
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
//      db.insertServiceInfo(serviceId);
      db.commit();
      return findServiceById(serviceId);
    } catch (Exception e) {
      db.rollback();
      throw new RuntimeException(e);
    }
  }

  @Override
  public Map<String, Object> findServiceById(final long serviceId) {
    return db.withHandle(new HandleCallback<Map<String, Object>>() {
      @Override
      public Map<String, Object> withHandle(Handle handle) throws Exception {
        Map<String, Object> service = handle.createQuery("SELECT s.*, c.status AS actual_status, false AS is_draft\n" +
            "FROM services AS s LEFT JOIN service_connections AS c ON s.id=c.service_id  WHERE s.id=:id")
            .bind("id", serviceId)
            .first();
        if (service == null) {
          service = handle.createQuery("SELECT\n" +
              "  s.service_id AS id\n" +
              "  , s.customer_id\n" +
              "  , s.service_name AS name\n" +
              "  , s.service_price AS price\n" +
              "  , s.service_download AS download\n" +
              "  , s.service_upload AS upload\n" +
              "  , '' AS info\n" +
              "  , 'ACTIVE' AS status\n" +
              "  , c.status AS actual_status\n" +
              "  , true is_draft\n" +
              "FROM service_drafts AS s\n" +
              "LEFT JOIN service_connections AS c ON s.service_id=c.service_id  WHERE s.service_id=:id")
              .bind("id", serviceId)
              .first();
        }
        if (service != null) {
          final Map<String, Object> draft = handle.createQuery("SELECT data FROM " + DRAFT_TABLE + " WHERE entity_type='services' " +
              "AND entity_id=:id")
              .bind("id", serviceId)
              .first();
          if (!(draft == null || draft.isEmpty())) {
            final Object data = draft.get("data");
            if (data != null) {
              final Map dataMap = new ObjectMapper().readValue(data.toString(), Map.class);
              String country = "";
              final Object countryId = dataMap.get("location_country");
              if (countryId != null) {
                for (Map.Entry<String, Long> countryEntry : COUNTRIES.entrySet()) {
                  if (countryEntry.getValue().toString().equals(countryId)) {
                    country = countryEntry.getKey();
                  }
                }
              }
              final Map<String, Object> address = new HashMap<>();
              address.put("street", dataMap.get("location_street"));
              address.put("descriptive_number", dataMap.get("location_descriptive_number"));
              address.put("orientation_number", dataMap.get("location_orientation_number"));
              address.put("apartment", dataMap.get("location_flat"));
              address.put("town", dataMap.get("location_town"));
              address.put("postal_code", dataMap.get("location_postal_code"));
              address.put("country", country);
              service.put("address", address);
            }
          }
          final Map<String, Object> productChannel = handle.createQuery("SELECT channel FROM " + PRODUCT_TABLE + " WHERE name like :name")
                .bind("name", service.get("name") + "%")
                .first();
          if (!(productChannel == null || productChannel.isEmpty())) {
            service.put("channel", productChannel.get("channel"));
          }
        }
        return service;
      }
    });
  }

  @Override
  public List<Map<String, Object>> findService(
      final String rawQuery,
      final String country,
      final Boolean isActive
  ) {
    final String query = Utils.replaceChars(rawQuery, TRANSLATE_FROM_CHARS, TRANSLATE_TO_CHARS);
    if (query.isEmpty()) return Lists.newArrayList();
    final Long countryId = COUNTRIES.get(country.toUpperCase());
    final String countryRestriction = countryId != null ? "c.country = " + countryId : "1 = 1";
    final String isActiveRestriction = isActive != null ? "c.is_active = " + isActive : "1 = 1";
    return db.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        return handle.createQuery(
            "SELECT\n" +
                "       a.id AS agreement_id\n" +
                "       , a.id % 100000 AS agreement\n" +
                "       , c.id AS customer_id\n" +
                "       , c.name AS customer_name\n" +
                "       , c.street\n" +
                "       , c.city\n" +
                "       , c.info as customer_info\n" +
                "       , s.id AS service_id\n" +
                "       , s.name AS service_name\n" +
                "       , s.download\n" +
                "       , s.upload\n" +
                "       , s.price\n" +
                "       , s.info AS service_info\n" +
                "       , false AS is_draft\n" +
                "FROM services AS s\n" +
                "  INNER JOIN customers AS c ON s.customer_id = c.id\n" +
                "  INNER JOIN agreements AS a ON s.id/100 = a.id\n" +
                "  LEFT JOIN pppoe AS p ON s.id = p.service_id\n" +
                "WHERE " + countryRestriction + "\n" +
                "AND   " + isActiveRestriction + "\n" +
                "AND   (lower(translate(c.name, :fromChars, :toChars)) ~* :query\n" +
                "  OR s.id\\:\\:text ~ :query\n" +
                "  OR lower(translate(p.interface, '-', '')) ~* :query\n" +
                "  OR lower(translate(p.location, '-', '')) ~* :query\n" +
                "  OR lower(translate(p.mac\\:\\:text, '\\:', '')) ~* :query\n" +
                "  OR (a.id % 100000)\\:\\:text ~ :query)\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT\n" +
                "     d.agreement_id\n" +
                "     , d.agreement\n" +
                "     , d.customer_id\n" +
                "     , d.customer_name\n" +
                "     , d.street\n" +
                "     , d.city\n" +
                "     , d.customer_info\n" +
                "     , d.service_id\n" +
                "     , d.service_name\n" +
                "     , d.service_download\n" +
                "     , d.service_upload\n" +
                "     , d.service_price\n" +
                "     , '' AS service_info\n" +
                "     , true AS is_draft\n" +
                "FROM service_drafts AS d\n" +
                "WHERE (lower(translate(d.customer_name, :fromChars, :toChars)) ~* :query\n" +
                "  OR d.service_id\\:\\:text ~ :query\n" +
                "  OR lower(translate(d.street, '-', '')) ~* :query\n" +
                "  OR lower(translate(d.city, '-', '')) ~* :query\n" +
                "  OR (d.agreement)\\:\\:text ~ :query)\n" +
                "\n" +
                "ORDER BY customer_name, service_id \n" +
                "LIMIT 25")
            .bind("query", query)
            .bind("fromChars", TRANSLATE_FROM_CHARS)
            .bind("toChars", TRANSLATE_TO_CHARS)
            .list();
      }
    });
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
  public void deleteService(final long serviceId) {
    db.withHandle(new HandleCallback<Object>() {
      @Override
      public Object withHandle(Handle handle) throws Exception {
        handle.begin();
        try {
          handle.createStatement("DELETE from services WHERE id=:id")
              .bind("id", serviceId)
              .execute();
          handle.createStatement("DELETE from services_info WHERE service_id=:service_id")
              .bind("service_" +
                  "id", serviceId)
              .execute();
          handle.commit();
        } catch (Exception e) {
          handle.rollback();
          throw new RuntimeException(e);
        }
        return null;
      }
    });
  }

  @Override
  public Map<String, Object> updateService(final long serviceId, final Map<String, Object> update) {
    dbi.inTransaction(new TransactionCallback<Void>() {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        updateRecordWithId(new Databases.RecordId("services", "id", serviceId), update, handle);
        return null;
      }
    });
    logger.info("service '{}' has been updated", serviceId);
    return findServiceById(serviceId);
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
      logger.debug("nothing to update returning original connection for service '{}'", serviceId);
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

  @Override
  public void deleteConnection(final long serviceId) {
    db.withHandle(new HandleCallback<Object>() {
      @Override
      public Object withHandle(Handle handle) throws Exception {
        handle.createStatement("DELETE from connections WHERE service_id=:service_id")
            .bind("service_id", serviceId)
            .execute();
        return null;
      }
    });
  }

  @Override
  public List<Map<String, Object>> findUserSubordinates(final String login) {
    return db.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        Long managerId = handle.createQuery("SELECT id FROM users where login=:login")
            .bind("login", login)
            .map(LongMapper.FIRST)
            .first();
        return handle.createQuery("SELECT id, login, name, full_name, roles, operation_country " +
            "FROM users WHERE reports_to=:manager_id")
            .bind("manager_id", managerId)
            .list();
      }
    });
  }

  @Override
  public Map<String, Object> findUserByLogin(final String login) {
    return db.withHandle(new HandleCallback<Map<String, Object>>() {
      @Override
      public Map<String, Object> withHandle(Handle handle) throws Exception {
        return handle
            .createQuery(
                "SELECT id, login, name, full_name, roles, operation_country FROM users where " +
                    "login=:login"
            )
            .bind("login", login)
            .first();
      }
    });
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

    @SqlQuery("SELECT id FROM agreements WHERE country=:country AND status='AVAILABLE' ORDER BY id LIMIT 1")
    long reusableAgreementIdByCountry(@Bind("country") String country);

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

    @SqlUpdate("UPDATE agreements SET customer_id=:customer_id WHERE id=:agreement_id")
    int updateAgreementCustomer(@Bind("agreement_id") long agreementId, @Bind("customer_id") long customerId);

    @SqlUpdate("UPDATE agreements SET status=:status WHERE id=:agreement_id")
    int updateAgreementStatus(@Bind("agreement_id") long agreementId, @Bind("status") String status);

    @SqlUpdate("UPDATE customers SET contract_no=:agreements WHERE id=:id")
    void setCustomerAgreements(@Bind("id") long customerId, @Bind("agreements") String agreements);

    @SqlUpdate("INSERT INTO services (id, customer_id, period_from, name, price) " +
        "VALUES (:service_id, :customer_id, :period_from, 'DRAFT', 0)")
    void insertService(@Bind("service_id") long serviceId, @Bind("customer_id") long customerId,
                       @Bind("period_from") Timestamp periodFrom);

    @SqlUpdate("INSERT INTO services_info (service_id, status, other_info) " +
        "VALUES (:service_id, 'DRAFT', '{}')")
    void insertServiceInfo(@Bind("service_id") long serviceId);

    @SqlUpdate("INSERT INTO connections (service_id) VALUES (:service_id)")
    void insertConnection(@Bind("service_id") long serviceId);

    @SqlUpdate("UPDATE services_info SET status=:status WHERE service_id=:service_id")
    void updateServiceStatus(@Bind("service_id") long serviceId, @Bind("status") String status);
  }
}
