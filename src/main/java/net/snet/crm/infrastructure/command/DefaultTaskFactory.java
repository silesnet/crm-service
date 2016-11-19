package net.snet.crm.infrastructure.command;

import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.command.Command;
import net.snet.crm.domain.shared.event.EventLog;
import org.skife.jdbi.v2.DBI;

public class DefaultTaskFactory implements TaskFactory {
  private final DBI dbi;
  private final NetworkService networkService;
  private final EventLog eventLog;

  public DefaultTaskFactory(DBI dbi, NetworkService networkService, EventLog eventLog) {
    this.dbi = dbi;
    this.networkService = networkService;
    this.eventLog = eventLog;
  }

  @Override
  public Task of(final Command command) {
    switch (command.name()) {
      case DISCONNECT:
        return new DisconnectCustomerTask(dbi, networkService, command, eventLog);
      default:
        throw new UnsupportedOperationException();
    }
  }
}
