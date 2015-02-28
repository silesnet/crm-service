package net.snet.crm.infrastructure.persistence.jdbi;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.snet.crm.domain.model.agreement.Agreement;
import net.snet.crm.domain.model.agreement.AgreementRepository;
import net.snet.crm.domain.model.agreement.Customer;
import net.snet.crm.domain.model.agreement.Service;
import net.snet.crm.domain.shared.Id;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.snet.crm.service.utils.Databases.getRecord;
import static net.snet.crm.service.utils.Databases.insertRecordWithoutKey;

public class DbiAgreementRepository implements AgreementRepository {
  private static final Logger log = LoggerFactory.getLogger(DbiAgreementRepository.class);

  public static final String SERVICES_TABLE = "services";
  private final DBI dbi;
  private final ObjectMapper mapper;

  public DbiAgreementRepository(final DBI dbi, final ObjectMapper mapper) {
    this.dbi = dbi;
    this.mapper = mapper;
  }

  @Override
  public Agreement add(Agreement entity) {
    return null;
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
  public Customer addCustomer(Customer customer) {
    return null;
  }
}
