package net.snet.crm.domain.model.network;

import java.util.List;
import java.util.Map;

public interface NetworkRepository {
  enum Country { CZ, PL }
  enum DeviceType { SWITCH, ROUTER }

  List<Map<String, Object>> findDevicesByCountryAndType(Country country, DeviceType deviceType);

  Map<String, Object> findDevice(int deviceId);

  Map<String, Object> findServiceDhcp(long serviceId);

  Map<String, Object> findServicePppoe(long serviceId);

  void bindDhcp(long serviceId, int switchId, int port);

  void disableDhcp(int switchId, int port);
}
