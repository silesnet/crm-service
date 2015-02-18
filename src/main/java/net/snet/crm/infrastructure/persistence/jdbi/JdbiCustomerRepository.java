package net.snet.crm.infrastructure.persistence.jdbi;

import net.snet.crm.domain.model.agreement.Customer;
import net.snet.crm.domain.model.agreement.CustomerRepository;
import net.snet.crm.domain.shared.Id;

public class JdbiCustomerRepository implements CustomerRepository {
  @Override
  public Customer add(Customer entity) {
    return null;
  }

  @Override
  public Customer get(Id<Customer> id) {
    return null;
  }

  @Override
  public Customer update(Customer entity) {
    return null;
  }
}
