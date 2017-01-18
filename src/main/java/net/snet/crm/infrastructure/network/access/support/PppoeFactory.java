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

  public Data pppoeOf(Data draft)
  {
    final Map<String, Object> pppoe = Maps.newHashMap();
    final Data data = draft.optionalDataOf("data");
    if (data.optionalStringOf("auth_a").isEmpty() &&
        data.optionalStringOf("password").isEmpty()) {
      return MapData.EMPTY;
    }
    pppoe.put("login", data.optionalStringOf("auth_a"));
    pppoe.put("password", data.optionalStringOf("auth_b"));
    pppoe.put("mac", macOf(data.optionalStringOf("mac_address")).asMap());
    pppoe.put("mode", data.optionalStringOf("product_channel"));
    pppoe.putAll(
        ipOf(
            data.optionalStringOf("ip"),
            data.longOf("service_id")
        ).asMap()
    );
    pppoe.putAll(
        deviceOf(data)
            .asMap()
    );
    return MapData.of(pppoe);
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
    final String channel = data.optionalStringOf("product_channel");
    if (channel.length() == 0) {
      return MapData.EMPTY;
    }
    final Map<String, Object> record = Maps.newHashMap();
    if ("LAN".equals(channel) || "FIBER".equals(channel)) {
      record.put("interface", "");
      final Data router = findDevice(data.optionalIntOf("core_router", 0));
      record.put("master", router.optionalStringOf("name"));
    } else {
      final Data ssid = findDevice(data.optionalIntOf("ssid", 0));
      record.put("interface", ssid.optionalStringOf("name"));
      record.put("master", ssid.optionalStringOf("master"));
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
