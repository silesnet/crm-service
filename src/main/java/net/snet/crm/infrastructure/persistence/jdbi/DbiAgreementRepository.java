package net.snet.crm.infrastructure.persistence.jdbi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.snet.crm.domain.model.agreement.Agreement;
import net.snet.crm.domain.model.agreement.AgreementRepository;
import net.snet.crm.domain.model.agreement.Customer;
import net.snet.crm.domain.model.agreement.Service;
import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.model.draft.DraftId;
import net.snet.crm.domain.shared.Id;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.domain.model.draft.Draft.Entity.SERVICES;
import static net.snet.crm.domain.model.draft.Draft.Status.APPROVED;
import static net.snet.crm.domain.model.draft.Draft.Status.IMPORTED;
import static net.snet.crm.service.utils.Databases.*;

public class DbiAgreementRepository implements AgreementRepository {
  private static final Logger log = LoggerFactory.getLogger(DbiAgreementRepository.class);

  public static final String AGREEMENTS_TABLE = "agreements";
  public static final String CUSTOMERS_TABLE = "customers";
  public static final String SERVICES_TABLE = "services";
  public static final String DRAFTS_TABLE = "drafts2";
  private static final String AUDIT_TABLE = "audit_items";
  private static final String AUDIT_ITEM_ID_SEQ = "audit_item_id_seq";

  private final DBI dbi;
  private final ObjectMapper mapper;

  public DbiAgreementRepository(final DBI dbi, final ObjectMapper mapper) {
    this.dbi = dbi;
    this.mapper = mapper;
  }

  @Override
  public void addService(final Optional<Draft> customerDraftOptional,
                         final Optional<Draft> agreementDraftOptional,
                         final Draft serviceDraft) {
    checkDraftEntityIs(SERVICES, serviceDraft);
    checkDraftStatusIs(APPROVED, serviceDraft);
    dbi.inTransaction(new TransactionCallback<Void>() {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        if (customerDraftOptional.isPresent()) {
          final Draft customerDraft = customerDraftOptional.get();
          checkDraftStatusIs(APPROVED, customerDraft);
          insertCustomerOf(customerDraft, handle);
          updateDraftStatusTo(IMPORTED, customerDraft.id(), handle);
        }
        if (agreementDraftOptional.isPresent()) {
          final Draft agreementDraft = agreementDraftOptional.get();
          checkDraftStatusIs(APPROVED, agreementDraft);
          insertAgreementOf(agreementDraft, handle);
          updateDraftStatusTo(IMPORTED, agreementDraft.id(), handle);
        }
        insertServiceOf(serviceDraft, handle);
        updateDraftStatusTo(IMPORTED, serviceDraft.id(), handle);
        updateCustomerStatusOf(
            customerDraftOptional,
            agreementDraftOptional,
            serviceDraft,
            handle
        );
        return null;
      }
    });
  }

  private void updateCustomerStatusOf(
      final Optional<Draft> customerDraftOptional,
      final Optional<Draft> agreementDraftOptional,
      final Draft serviceDraft,
      final Handle handle) {
    final Map<String, Object> customerUpdate = Maps.newHashMap();
    final long customerId;
    final Service service = new Service(serviceDraft);
    if (customerDraftOptional.isPresent() && agreementDraftOptional.isPresent()) {
      customerId = customerDraftOptional.get().entityId();
      final Draft agreementDraft = agreementDraftOptional.get();
      final Agreement agreement = new Agreement(agreementDraft);
      if ("PL".equals(agreementDraft.entitySpate())) {
        final Object symbol = "PL-" + (agreementDraft.entityId() - 200000);
        customerUpdate.put("symbol", symbol);
      }
      final Object variable = agreement.number();
      customerUpdate.put("lastly_billed", service.periodStart().dayOfMonth().withMaximumValue());
      customerUpdate.put("status", 20);
      customerUpdate.put("variable", variable);
    }
    else {
      customerId = service.customerId();
    }
    customerUpdate.put("customer_status", "ACTIVE");
    customerUpdate.put("is_active", true);
    updateRecord(CUSTOMERS_TABLE, customerId, customerUpdate, handle);
  }

  private void checkDraftEntityIs(final Draft.Entity entity, final Draft draft) {
    checkState(entity.equals(draft.entity()),
        "expected '%s' draft, but got '%s'", entity, draft.entity());
  }

  private void checkDraftStatusIs(final Draft.Status status, final Draft draft) {
    checkState(status.equals(draft.status()),
        "expected '%s' service draft status, but got '%s'", status, draft.status());
  }

  @Override
  public Agreement add(final Agreement agreement) {
    return dbi.withHandle(new HandleCallback<Agreement>() {
      @Override
      public Agreement withHandle(Handle handle) throws Exception {
        log.debug("inserting agreement '{}'", agreement.id().value());
        insertRecordWithoutKey(AGREEMENTS_TABLE, agreement.record(), handle);
        return new Agreement(getRecord(AGREEMENTS_TABLE, agreement.id().value(), handle));
      }
    });
  }

  @Override
  public Agreement get(Id<Agreement> id) {
    return null;
  }

  @Override
  public Agreement update(Agreement entity) {
    return null;
  }

  @Override
  public Service addService(final Service service) {
    return dbi.withHandle(new HandleCallback<Service>() {
      @Override
      public Service withHandle(Handle handle) throws Exception {
        log.debug("inserting service '{}'", service.id().value());
        insertRecordWithoutKey(SERVICES_TABLE, service.record(), handle);
        return new Service(getRecord(SERVICES_TABLE, service.id().value(), handle));
      }
    });
  }

  @Override
  public Customer addCustomer(final Customer customer) {
    return dbi.withHandle(new HandleCallback<Customer>() {
      @Override
      public Customer withHandle(Handle handle) throws Exception {
        log.debug("inserting service '{}'", customer.id().value());
        final Map<String, Object> record = Maps.newHashMap(customer.record());
        final DateTime now = DateTime.now();
        record.put("inserted_on", now);
        record.put("updated", now);
        record.put("history_id", lastValueOf(AUDIT_TABLE, "history_id", handle) + 1);
        // TODO: insert audit item to history
        insertRecordWithoutKey(CUSTOMERS_TABLE, record, handle);
        return new Customer(getRecord(CUSTOMERS_TABLE, customer.id().value(), handle));
      }
    });
  }

  private void insertCustomerOf(final Draft draft, final Handle handle) {
    log.debug("adding new customer '{}' from draft '{}'",
        draft.entityId(), draft.id().value());
    final Customer customer = new Customer(draft);
    final Map<String, Object> record = Maps.newHashMap(customer.record());
    final DateTime now = DateTime.now();
    record.put("inserted_on", now);
    record.put("updated", now);
    final long historyId = insertCustomerAuditOf(draft, handle);
    record.put("history_id", historyId);
    insertRecordWithoutKey(CUSTOMERS_TABLE, record, handle);
  }

  private long insertCustomerAuditOf(final Draft draft, final Handle handle) {
    final long auditId = nextValOf(AUDIT_ITEM_ID_SEQ, handle);
    final long historyId = lastValueOf(AUDIT_TABLE, "history_id", handle) + 1;
    final Map<String, Object> record = ImmutableMap.<String, Object>builder()
        .put("id", auditId)
        .put("history_id", historyId)
        .put("history_type_label_id", 41L) // customer audit label id
        .put("user_id", 2L) // system user
        .put("time_stamp", DateTime.now())
        .put("field_name", "name")
        .put("old_value", Optional.absent())
        .put("new_value", draft.entityName())
        .build();
    insertRecordWithoutKey(AUDIT_TABLE, record, handle);
    return historyId;
  }

  private void insertServiceOf(final Draft draft, final Handle handle) {
    log.debug("adding new service '{}' from draft '{}'", draft.entityId(), draft.id().value());
    final Service service = new Service(draft);
    insertRecordWithoutKey(SERVICES_TABLE, service.record(), handle);
  }

  private void insertAgreementOf(final Draft draft, final Handle handle) {
    log.debug("adding new agreement '{}' from draft '{}'", draft.entityId(), draft.id().value());
    final Agreement agreement = new Agreement(draft);
    insertRecordWithoutKey(AGREEMENTS_TABLE, agreement.record(), handle);
  }

  private void updateDraftStatusTo(final Draft.Status status, final DraftId id, final Handle handle) {
    log.debug("updating status get draft '{}' to '{}'", id.value(), status);
    final Map<String, Object> update = ImmutableMap.<String, Object>of("status", status.toString());
    updateRecord(DRAFTS_TABLE, id.value(), update, handle);
  }

}
