package net.snet.crm.domain.model.agreement;

import com.google.common.base.Optional;
import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.shared.Repository;

public interface AgreementRepository extends Repository<Agreement> {
  Service addService(Service service);

  Customer addCustomer(Customer customer);

  void addService(final Optional<Draft> customerDraft,
                  final Optional<Draft> agreementDraft,
                  final Draft serviceDraft);
}
