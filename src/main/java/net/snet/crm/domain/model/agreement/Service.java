package net.snet.crm.domain.model.agreement;

import net.snet.crm.domain.shared.Entity;

import java.util.Map;

public class Service implements Entity<Service, ServiceId> {
  public Service(Map<String, Object> draft) {

  }

  @Override
  public ServiceId id() {
    return null;
  }
}
