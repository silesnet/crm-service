package net.snet.crm.domain.model.agreement;

import net.snet.crm.service.bo.Customer;
import net.snet.crm.domain.shared.GenericLongId;

public class CustomerId extends GenericLongId<Customer> {
  public CustomerId(long id) {
    super(id);
  }

  public CustomerId() {
  }
}
