package net.snet.crm.infrastructure.persistence.jdbi;

import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
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
import static net.snet.crm.service.utils.Entities.valueMapOf;

public class DbiNetworkRepository implements NetworkRepository {
  private static final Logger logger = LoggerFactory.getLogger(DbiNetworkRepository.class);
  private static final String DHCP_TABLE = "dhcp";
  private static final String DHCP_WIRELESS_TABLE = "dhcp_wireless";
  private static final String PPPOE_TABLE = "pppoe";
  private static final String NETWORK_TABLE = "network";
  private static final String LOGIP_TABLE = "radlogip";
  public static final Map<String, String> EMPTY_MAC = ImmutableMap.of("type", "macaddr", "value", "");
  public static final Map<String, String> EMPTY_IP = ImmutableMap.of("type", "inet", "value", "");
  private final DBI dbi;

  public DbiNetworkRepository(DBI dbi) {
    this.dbi = dbi;
  }

  @Override
  public List<Map<String, Object>> findConflictingAuthentications() {
    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        return handle.createQuery(
            "SELECT s.*\n" +
                "       , p.*\n" +
                "       , d.*\n" +
                "FROM services AS s\n" +
                "  LEFT JOIN pppoe AS p ON s.id = p.service_id\n" +
                "  LEFT JOIN dhcp AS d ON s.id = d.service_id\n" +
                "WHERE p.service_id IS NOT NULL\n" +
                "AND   d.service_id IS NOT NULL")
            .list();
      }
    });
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
        "SELECT d.*, n.name as switch, n.master FROM " + DHCP_TABLE + " d " +
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
        pppoe.put("ip", EMPTY_IP);
      }
      if (pppoe.get("mac") == null) {
        pppoe.put("mac", EMPTY_MAC);
      }
      pppoe.put("radius", getRecord(
          "SELECT * FROM radius WHERE id=:serviceId",
          ImmutableMap.of("serviceId", (Object) serviceId),
          dbi
      ).or(new HashMap<String, Object>()));
      if (pppoe.get("interface") != null) {
        pppoe.put("network_interface", getRecord(
            "SELECT id, name, ssid, master  FROM " + NETWORK_TABLE + " WHERE name=:name",
            ImmutableMap.of("name", pppoe.get("interface")),
            dbi
        ).or(new HashMap<String, Object>()));
      }
      if (pppoe.get("master") != null) {
        pppoe.put("network_master", getRecord(
            "SELECT id, name, ssid, master  FROM " + NETWORK_TABLE + " WHERE name=:name",
            ImmutableMap.of("name", pppoe.get("master")),
            dbi
        ).or(new HashMap<String, Object>()));
      }
    }
    return pppoe;
  }

  @Override
  public List<Map<String, Object>> findPppoeUserLastIp(String login) {
    return findRecords(
        "SELECT * FROM " + LOGIP_TABLE + " WHERE username=:login ORDER BY date DESC LIMIT 4;",
        ImmutableMap.of("login", (Object) login),
        dbi);
  }

  @Override
  public Data findServiceDhcpWireless(long serviceId) {
    Data record = findRecord(dhcpWirelessRecordIdOf(serviceId), dbi);
    if (!record.isEmpty()) {
      if (!record.hasValue("mac") || !record.hasValue("ip")) {
        final Map<String, Object> map = record.asMap();
        if (!record.hasValue("mac")) {
          map.put("mac", EMPTY_MAC);
        }
        if (!record.hasValue("ip")) {
          map.put("ip", EMPTY_IP);
        }
        record = MapData.of(map);
      }
    }
    return record;
  }

  @Override
  public void addDhcpWireless(long serviceId, Data dhcp) {
    logger.debug("adding DHCP wireless for service '{}'", serviceId);
    if (dhcp.hasPath("service_id")) {
      checkState(dhcp.longOf("service_id") == serviceId,
                 "trying to add wireless DHCP of different service '%s'",
                 dhcp.longOf("service_id"));
    } else {
      final Map<String, Object> map = dhcp.asMap();
      map.put("service_id", serviceId);
      dhcp = MapData.of(map);
    }
    insertRecordWithoutKey(DHCP_WIRELESS_TABLE, dhcp, dbi);
    logger.info("added DHCP wireless for service '{}'", serviceId);
  }

  @Override
  public void updateDhcpWireless(long serviceId, Data update) {
    logger.debug("updating DHCP wireless for service '{}'", serviceId);
    if (update.hasPath("service_id")) {
      checkState(update.longOf("service_id") == serviceId,
                 "trying to update wireless DHCP of different service '%s'",
                 update.longOf("service_id"));
    }
    updateRecord(dhcpWirelessRecordIdOf(serviceId), update, dbi);
    logger.info("updated DHCP wireless for service '{}'", serviceId);
  }

  @Override
  public void removeDhcpWireless(long serviceId) {
    logger.debug("removing DHCP wireless for service '{}'", serviceId);
    final boolean deleted = deleteRecord(dhcpWirelessRecordIdOf(serviceId), dbi);
    if (deleted) {
      logger.info("removed DHCP wireless for service '{}'", serviceId);
    } else {
      logger.info("DHCP wireless for service '{}' not found", serviceId);
    }
  }

  private RecordId dhcpWirelessRecordIdOf(long serviceId)
  {
    return RecordId.of(DHCP_WIRELESS_TABLE, "service_id", serviceId);
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
    pppoe.put("service_id", serviceId);
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
        removePppoe(serviceId, handle);
        return null;
      }
    });
  }

  @Override
  public void removePppoe(final long serviceId, Handle handle) {
    handle.createStatement("DELETE FROM " + PPPOE_TABLE + " WHERE service_id=:service_id")
        .bind("service_id", serviceId)
        .execute();
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
      checkState(updatedCount > 0, "failed on updated dhcp record for " +
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
