package net.snet.crm.domain.model.agreement;

import net.snet.crm.domain.shared.Repository;

public interface AgreementRepository extends Repository<Agreement> {
  Service addService(Service service);

  Customer addCustomer(Customer customer);
}
