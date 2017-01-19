package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.infrastructure.network.access.Action;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BaseAction implements Action
{
  static final Logger log = LoggerFactory.getLogger(BaseAction.class);

  final NetworkRepository networkRepository;
  final NetworkService networkService;
  final List<String> messages;
  long serviceId;
  Data draft;
  Handle handle;

  public BaseAction(NetworkRepository networkRepository, NetworkService networkService) {
    this.networkRepository = networkRepository;
    this.networkService = networkService;
    this.messages = new ArrayList<>();
  }

  @Override
  final public List<String> perform(long serviceId, Data draft, Handle handle) {
    populateFields(serviceId, draft, handle);
    try {
      if (!initialize()) {
        log.warn(
            "initialization of access transition action for service '{}' short circuit: '{}'",
            serviceId,
            messages);
        return messages;
      }
      updateDatabase();
    } catch (Exception e) {
      throw new RuntimeException(
          String.format("database update of access protocol failed for service '%s': %s",
                        serviceId,
                        e.getMessage()),
          e
      );
    }
    try {
      updateNetwork();
    } catch (Exception e) {
      appendMessage("warning: network changes failed: %s", e.getMessage());
    }
    clearFields();
    return messages;
  }

  boolean initialize() {
    return true;
  }

  void updateDatabase() {
  }

  void updateNetwork() {
  }

  final void appendMessage(String template, Object... data) {
    messages.add(
        String.format(template, data)
    );
  }

  private void populateFields(long serviceId, Data draft, Handle handle) {
    this.serviceId = serviceId;
    this.draft = draft;
    this.handle = handle;
  }

  private void clearFields() {
    this.serviceId = -1;
    this.draft = null;
    this.handle = null;
  }
}
