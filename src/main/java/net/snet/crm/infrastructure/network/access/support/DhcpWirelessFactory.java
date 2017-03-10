package net.snet.crm.infrastructure.network.access.support;

import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

import java.util.Map;

public class DhcpWirelessFactory
{
  private final NetworkRepository networkRepository;

  public DhcpWirelessFactory(NetworkRepository networkRepository) {
    this.networkRepository = networkRepository;
  }

  public DhcpWireless dhcpWirelessOf(Data draft) {
    final Data data = draft.optDataOf("data");
    final Map<String, Object> dhcpWireless = Maps.newHashMap();
    dhcpWireless.put("mac", macOf(data.optStringOf("mac_address")).asMap());
    dhcpWireless.putAll(interfaceAndMasterOf(data.optIntOf("ssid")).asMap());
    dhcpWireless.putAll(
        ipOf(
            data.optStringOf("ip"),
            data.longOf("service_id")
        ).asMap()
    );
    return DhcpWireless.of(MapData.of(dhcpWireless));
  }

  public DhcpWireless dhcpWirelessOf(long serviceId) {
    final Data data = networkRepository.findServiceDhcpWireless(serviceId);
    if (data.isEmpty()) {
      return DhcpWireless.NULL;
    }
    return DhcpWireless.of(data);
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
      ipRecord.put("value", "");
      ipClass = ip.length() > 0 ? ip : defaultIpClass(serviceId);
    }

    final Map<String, Object> record = Maps.newHashMap();
    record.put("ip", ipRecord);
    record.put("ip_class", ipClass);
    return MapData.of(record);
  }


  private String defaultIpClass(long serviceId)
  {
    return ("" + serviceId).startsWith("1")
        ? "internal-cz"
        : "public-pl";
  }

  private Data interfaceAndMasterOf(int id)
  {
    final Map<String, Object> record = Maps.newHashMap();
    final Data device = findDevice(id);
    record.put("interface", device.optStringOf("name"));
    record.put("master", device.optStringOf("master"));
    return MapData.of(record);
  }

  private Data findDevice(int id)
  {
    return MapData.of(networkRepository.findDevice(id));
  }

}
