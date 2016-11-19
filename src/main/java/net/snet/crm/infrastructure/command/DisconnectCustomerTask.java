package net.snet.crm.infrastructure.command;

import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.command.Command;
import net.snet.crm.domain.shared.event.EventLog;
import net.snet.crm.service.utils.Databases;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.util.LongMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static net.snet.crm.domain.shared.command.Commands.DISCONNECT;
import static net.snet.crm.domain.shared.event.Event.occurred;
import static net.snet.crm.domain.shared.event.Events.DISCONNECTED;
import static net.snet.crm.service.utils.Databases.updateRecord;
import static net.snet.crm.service.utils.Databases.updateRecords;

public class DisconnectCustomerTask implements Task {
  private final static Logger log = LoggerFactory.getLogger(DisconnectCustomerTask.class);
  private static final String CUSTOMERS = "customers";
  private static final String SERVICES = "services";

  private final DBI dbi;
  private final NetworkService networkService;
  private final Command command;
  private final EventLog eventLog;

  public DisconnectCustomerTask(DBI dbi, NetworkService networkService, Command command, EventLog eventLog) {
    this.dbi = dbi;
    this.networkService = networkService;
    checkArgument(DISCONNECT.equals(command.name()), "trying disconnect customer with wrong command '%s'", command.name());
    checkArgument(CUSTOMERS.equals(command.entity()), "tyring on disconnect non 'customers' entity '%s'", command.entity());
    this.command = command;
    this.eventLog = eventLog;
  }

  @Override
  public void perform() {
    log.debug("disconnecting customer with id '{}'", command.entityId());
    dbi.inTransaction(new TransactionCallback<Void>() {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        final Long customerId = Long.valueOf(command.entityId());
        updateRecord(CUSTOMERS, customerId, debtorStatus(), handle);
        updateRecords(customerServices(customerId), inheritStatus(), handle);
        for (Long serviceId : findCustomerServices(customerId, handle)) {
          networkService.disableService(serviceId);
          publishDisconnected(SERVICES, serviceId);
        }
        publishDisconnected(CUSTOMERS, customerId);
        return null;
      }
    });
    log.info("disconnected customer with id '{}'", command.entityId());
  }

  private void publishDisconnected(String entity, long entityId) {
    eventLog.publish(
        occurred(DISCONNECTED).on(entity, entityId).withCommandId(command.id()).build());
  }

  private Databases.RecordId customerServices(Long customerId) {
    return new Databases.RecordId(SERVICES, "customer_id", customerId);
  }

  private Map<String, Object> debtorStatus() {
    return ImmutableMap.<String, Object>of("status", 20);
  }

  private Map<String, Object> inheritStatus() {
    return ImmutableMap.<String, Object>of("status", "INHERIT_FROM_CUSTOMER");
  }

  private List<Long> findCustomerServices(Long customerId, Handle handle) {
    return handle.createQuery("SELECT id FROM services WHERE customer_id=:customerId;")
              .bind("customerId", customerId).map(LongMapper.FIRST).list();
  }
}
