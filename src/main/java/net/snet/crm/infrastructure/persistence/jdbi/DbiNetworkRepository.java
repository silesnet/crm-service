package net.snet.crm.infrastructure.persistence.jdbi;

import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.model.network.NetworkRepository;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.tweak.HandleCallback;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.domain.model.network.NetworkRepository.Country.PL;
import static net.snet.crm.service.utils.Databases.insertRecordWithoutKey;

public class DbiNetworkRepository implements NetworkRepository {
  private static final String DHCP_TABLE = "dhcp";
  private static final String NETWORK_TABLE = "network";
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
                "SELECT id, name FROM "+ NETWORK_TABLE +
                    " WHERE type = :type AND name ~ '^.+-br$' AND country = :country" +
                    " ORDER BY name")
            .bind("type", deviceTypeId)
            .bind("country", countryId)
            .list();
      }
    });
  }

  @Override
  public void enableDhcp(final long serviceId, final int switchId, final int port) {
    dbi.inTransaction(new TransactionCallback<Object>() {
      @Override
      public Object inTransaction(Handle handle, TransactionStatus status) throws Exception {
        final int existingCount = handle.createQuery(
            "SELECT service_id FROM " + DHCP_TABLE + " WHERE network_id=:switch_id AND port=:port")
            .bind("switch_id", switchId)
            .bind("port", port)
            .list()
            .size();
        if (existingCount > 0) {
          final int updatedCount = handle.createStatement(
              "UPDATE " + DHCP_TABLE + " SET service_id=:service_id" +
                  " WHERE network_id=:switch_id AND port=:port")
              .bind("service_id", serviceId)
              .bind("switch_id", switchId)
              .bind("port", port)
              .execute();
          checkState(updatedCount > 0, "failed to updated dhcp record for " +
              "network_id='%s' and port='%s'", switchId, port);
        } else {
          final Map<String, Object> record = ImmutableMap.<String, Object>builder()
              .put("service_id", serviceId)
              .put("network_id", switchId)
              .put("port", port)
              .build();
          insertRecordWithoutKey(DHCP_TABLE, record, handle);
        }
        return null;
      }
    });
  }

  @Override
  public void disableDhcp(int switchId, int port) {

  }
}
