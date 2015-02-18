package net.snet.crm.domain.model.agreement;

import net.snet.crm.domain.shared.Entity;

import java.util.Map;

public class Customer implements Entity<Customer, CustomerId> {
  final CustomerId id;
  final Map<String, Object> properties;

  public Customer(Map<String, Object> draft) {
    this.id = new CustomerId();
    // convert draft into customer properties
    this.properties = draft;
  }

  public Customer(CustomerId id, Map<String, Object> properties) {
    this.id = id;
    this.properties = properties;
  }

  @Override
  public CustomerId id() {
    return id;
  }
}
