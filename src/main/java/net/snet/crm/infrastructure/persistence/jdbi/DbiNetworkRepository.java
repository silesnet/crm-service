package net.snet.crm.infrastructure.persistence.jdbi;

import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.service.utils.Entities.ValueMap;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.LongMapper;
import org.skife.jdbi.v2.util.StringMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.domain.model.network.NetworkRepository.Country.PL;
import static net.snet.crm.service.utils.Databases.*;
import static net.snet.crm.service.utils.Databases.insertRecordWithoutKey;
import static net.snet.crm.service.utils.Databases.updateRecordWithId;
import static net.snet.crm.service.utils.Entities.valueMapOf;

public class DbiNetworkRepository implements NetworkRepository {
  private static final Logger logger = LoggerFactory.getLogger(DbiNetworkRepository.class);
  private static final String DHCP_TABLE = "dhcp";
  private static final String PPPOE_TABLE = "pppoe";
  private static final String NETWORK_TABLE = "network";
  private final DBI dbi;

  public DbiNetworkRepository(DBI dbi) {
    this.dbi = dbi;
  }

  @Override
  public List<String> findAllMasters() {
    return dbi.withHandle(new HandleCallback<List<String>>() {
      @Override
      public List<String> withHandle(Handle handle) throws Exception {
        return handle.createQuery("SELECT DISTINCT master FROM " + NETWORK_TABLE + " ORDER BY master;")
            .map(StringMapper.FIRST)
            .list();
      }
    });
  }

  @Override
  public Map<String, Object> findDevice(final int deviceId) {
    final Map<String, Object> device = dbi.withHandle(new HandleCallback<Map<String, Object>>() {
      @Override
      public Map<String, Object> withHandle(Handle handle) throws Exception {
        return handle.createQuery(
            "SELECT * FROM " + NETWORK_TABLE + " WHERE id=:id")
            .bind("id", deviceId)
            .first();
      }
    });
    return device != null ? device : ImmutableMap.<String, Object>of();
  }

  @Override
  public List<Map<String, Object>> findDevicesByCountryAndType(
      final Country country,
      final DeviceType deviceType) {
    final int countryId = Country.CZ.equals(country) ? 10 : PL.equals(country) ? 20 : 0;
    final int deviceTypeId = DeviceType.SWITCH.equals(deviceType) ? 40 : 0;
    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        return handle.createQuery(
            "SELECT id, name, master FROM " + NETWORK_TABLE +
                " WHERE type = :type AND name ~ '^.+-br.?.?$' AND country = :country" +
                " ORDER BY name")
            .bind("type", deviceTypeId)
            .bind("country", countryId)
            .list();
      }
    });
  }

  @Override
  public Map<String, Object> findServiceDhcp(final long serviceId) {
    return getRecord(
        "SELECT d.*, n.master FROM " + DHCP_TABLE + " d " +
            "LEFT JOIN " + NETWORK_TABLE + " n ON n.id=d.network_id " +
            "WHERE d.service_id=:serviceId",
        ImmutableMap.of("serviceId", (Object) serviceId),
        dbi
    ).or(new HashMap<String, Object>());
  }

  @Override
  public Map<String, Object> findServicePppoe(final long serviceId) {
    final Map<String, Object> pppoe = getRecord(
        "SELECT * FROM " + PPPOE_TABLE + " WHERE service_id=:serviceId",
        ImmutableMap.of("serviceId", (Object) serviceId),
        dbi
    ).or(new HashMap<String, Object>());
    if (!pppoe.isEmpty()) {
      if (pppoe.get("ip") == null) {
        pppoe.put("ip", ImmutableMap.of("type", "inet", "value", ""));
      }
      if (pppoe.get("mac") == null) {
        pppoe.put("mac", ImmutableMap.of("type", "macaddr", "value", ""));
      }
      pppoe.put("radius", getRecord(
          "SELECT * FROM radius WHERE id=:serviceId",
          ImmutableMap.of("serviceId", (Object) serviceId),
          dbi
      ).or(new HashMap<String, Object>()));
    }
    return pppoe;
  }

  @Override
  public void bindDhcp(final long serviceId, final int switchId, final int port) {
    dbi.inTransaction(new TransactionCallback<Object>() {
      @Override
      public Object inTransaction(Handle handle, TransactionStatus status) throws Exception {
        ValueMap currentDhcp = valueMapOf(handle
            .createQuery("SELECT network_id, port FROM " + DHCP_TABLE + " WHERE service_id=:serviceId")
            .bind("serviceId", serviceId)
            .first());
        if (currentDhcp.map() != null) {
          disableDhcpInternal(
              currentDhcp.get("network_id").asInteger(),
              currentDhcp.get("port").asInteger(),
              handle);
        }
        enableDhcpInternal(serviceId, switchId, port, handle);
        return null;
      }
    });
  }

  @Override
  public void disableDhcp(final int switchId, final int port) {
    dbi.inTransaction(new TransactionCallback<Void>() {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        disableDhcpInternal(switchId, port, handle);
        return null;
      }
    });
  }

  @Override
  public void addPppoe(long serviceId, final Map<String, Object> pppoe) {
    dbi.inTransaction(new TransactionCallback<Void>() {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        insertRecordWithoutKey(PPPOE_TABLE, pppoe, handle);
        return null;
      }
    });
  }

  @Override
  public void updateDhcp(final long serviceId, final Map<String, Object> update) {
    dbi.inTransaction(new TransactionCallback<Void>() {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        updateRecordWithId(new RecordId(DHCP_TABLE, "service_id", serviceId),
            update, handle);
        return null;
      }
    });
  }

  @Override
  public void removePppoe(final long serviceId) {
    dbi.inTransaction(new TransactionCallback<Void>() {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        handle.createStatement("DELETE FROM " + PPPOE_TABLE + " WHERE service_id=:service_id")
            .bind("service_id", serviceId)
            .execute();
        return null;
      }
    });
  }

  @Override
  public void updatePppoe(final long serviceId, final Map<String, Object> update) {
    dbi.inTransaction(new TransactionCallback<Void>() {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        updateRecordWithId(new RecordId(PPPOE_TABLE, "service_id", serviceId),
            update, handle);
        return null;
      }
    });
  }

  private void disableDhcpInternal(int switchId, int port, Handle handle) {
    final int updatedCount = handle.createStatement(
        "UPDATE " + DHCP_TABLE + " SET service_id=NULL" +
            " WHERE network_id=:switch_id AND port=:port")
        .bind("switch_id", switchId)
        .bind("port", port)
        .execute();
    if (updatedCount == 0) {
      logger.warn("DHCP switch/port '{}/{}' was not disabled, its missing in database",
          switchId, port);
    }
    logger.info("DHCP switch/port '{}/{}' was disabled", switchId, port);
  }

  private void enableDhcpInternal(long serviceId, int switchId, int port, Handle handle) {
    final Long currentServiceId = handle.createQuery(
        "SELECT service_id FROM " + DHCP_TABLE + " WHERE network_id=:switch_id AND port=:port")
        .bind("switch_id", switchId)
        .bind("port", port)
        .map(LongMapper.FIRST)
        .first();
    if (currentServiceId != null) {
      checkState(currentServiceId == 0 || currentServiceId == serviceId,
          "can't enable DHCP [%s, %s] for %s as it is already in use by %s",
          switchId, port, serviceId, currentServiceId);
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
    logger.info("DHCP switch/port '{}/{}' was enabled", switchId, port);
  }

}
