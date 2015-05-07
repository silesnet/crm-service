package net.snet.crm.infrastructure.persistence.jdbi;

import net.snet.crm.domain.model.network.NetworkRepository;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

import java.util.List;
import java.util.Map;

import static net.snet.crm.domain.model.network.NetworkRepository.Country.PL;

public class DbiNetworkRepository implements NetworkRepository {
  private final DBI dbi;

  public DbiNetworkRepository(DBI dbi) {
    this.dbi = dbi;
  }

  @Override
  public List<Map<String, Object>> findDevicesByCountryAndType(
      final Country country,
      final DeviceType deviceType)
  {
    final int countryId = Country.CZ.equals(country) ? 10 : PL.equals(country) ? 20 : 0;
    final int deviceTypeId = DeviceType.SWITCH.equals(deviceType) ? 40 : 0;
    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        return handle.createQuery(
                "SELECT id, name FROM network" + "" +
                    " WHERE type = :type AND name ~ '^.+-br$' AND country = :country")
            .bind("type", deviceTypeId)
            .bind("country", countryId)
            .list();
      }
    });
  }
}
