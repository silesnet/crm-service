package net.snet.crm.domain.model.network;

import net.snet.crm.domain.shared.data.Data;
import org.skife.jdbi.v2.Handle;

import java.util.List;
import java.util.Map;

public interface NetworkRepository {

  List<Map<String, Object>> findConflictingAuthentications();

  enum Country { CZ, PL }
  enum DeviceType { SWITCH, ROUTER }

  List<Map<String, Object>> findDevicesByCountryAndType(Country country, DeviceType deviceType);

  List<String> findAllMasters();

  Map<String, Object> findDevice(int deviceId);

  Map<String, Object> findServiceDhcp(long serviceId);

  Map<String, Object> findServicePppoe(long serviceId);

  List<Map<String, Object>> findPppoeUserLastIp(String login);

  Data findServiceDhcpWireless(long serviceId);

  void addDhcpWireless(long serviceId, Data dhcp);

  void updateDhcpWireless(long serviceId, Data update);

  void removeDhcpWireless(long serviceId);

  void bindDhcp(long serviceId, int switchId, int port);

  void disableDhcp(int switchId, int port);

  void updateDhcp(long serviceId, Map<String, Object> update);

  void addPppoe(long serviceId, Map<String, Object> pppoe);

  void updatePppoe(long serviceId, Map<String, Object> update);

  void removePppoe(long serviceId);

  void removePppoe(long serviceId, Handle handle);
}
