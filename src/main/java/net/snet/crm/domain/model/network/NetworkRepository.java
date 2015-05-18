package net.snet.crm.domain.model.network;

import java.util.List;
import java.util.Map;

public interface NetworkRepository {
  enum Country { CZ, PL }
  enum DeviceType { SWITCH, ROUTER }

  List<Map<String, Object>> findDevicesByCountryAndType(Country country, DeviceType deviceType);

  void enableDhcp(long serviceId, int switchId, int port);

  void disableDhcp(int switchId, int port);
}