package net.snet.crm.infrastructure.network.access.support;

import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

import java.util.Map;

public class PppoeFactory
{
  private final NetworkRepository networkRepository;

  public PppoeFactory(NetworkRepository networkRepository)
  {
    this.networkRepository = networkRepository;
  }

  public Pppoe pppoeOf(Data draft)
  {
    final Data data = draft.optDataOf("data");
    if (data.optStringOf("auth_a").isEmpty() &&
        data.optStringOf("auth_b").isEmpty()) {
      return Pppoe.NULL;
    }

    final Map<String, Object> pppoe = Maps.newHashMap();
    pppoe.put("login", data.optStringOf("auth_a"));
    pppoe.put("password", data.optStringOf("auth_b"));
    pppoe.put("mac", macOf(data.optStringOf("mac_address")).asMap());
    pppoe.put("mode", data.optStringOf("product_channel").toUpperCase());
    pppoe.putAll(
        ipOf(
            data.optStringOf("ip"),
            data.longOf("service_id")
        ).asMap()
    );
    pppoe.putAll(
        deviceOf(data)
            .asMap()
    );
    return Pppoe.of(MapData.of(pppoe));
  }

  public Pppoe pppoeOf(long serviceId) {
    final Data data = MapData.of(networkRepository.findServicePppoe(serviceId));
    if (data.isEmpty()) {
      return Pppoe.NULL;
    }
    return Pppoe.of(data);
  }

  private Data macOf(String mac)
  {
    final Map<String, Object> record = Maps.newHashMap();
    record.put("type", "macaddr");
    record.put("value", mac.length() > 0 ? mac : null);
    return MapData.of(record);
  }

  private Data ipOf(String ip, long serviceId)
  {
    final Map<String, Object> ipRecord = Maps.newHashMap();
    ipRecord.put("type", "inet");

    String ipClass;
    try {
      InetAddresses.forString(ip);
      ipRecord.put("value", ip);
      ipClass = "static";
    } catch (IllegalArgumentException e) {
      ipRecord.put("value", null);
      ipClass = ip.length() > 0 ? ip : defaultIpClass(serviceId);
    }

    final Map<String, Object> record = Maps.newHashMap();
    record.put("ip", ipRecord);
    record.put("ip_class", ipClass);
    return MapData.of(record);
  }

  private Data deviceOf(Data data)
  {
    final String channel = data.optStringOf("product_channel").toUpperCase();
    if (channel.length() == 0) {
      return MapData.EMPTY;
    }
    final Map<String, Object> record = Maps.newHashMap();
    if ("LAN".equals(channel) || "FIBER".equals(channel)) {
      record.put("interface", "");
      final Data router = findDevice(data.optIntOf("core_router", 0));
      record.put("master", router.optStringOf("name"));
    } else {
      final Data ssid = findDevice(data.optIntOf("ssid", 0));
      record.put("interface", ssid.optStringOf("name"));
      record.put("master", ssid.optStringOf("master"));
    }
    return MapData.of(record);
  }

  private Data findDevice(int id)
  {
    return MapData.of(networkRepository.findDevice(id));
  }

  private String defaultIpClass(long serviceId)
  {
    return ("" + serviceId).startsWith("1")
        ? "internal-cz"
        : "public-pl";
  }

}
