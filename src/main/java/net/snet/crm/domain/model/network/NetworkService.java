package net.snet.crm.domain.model.network;

import net.snet.crm.domain.shared.data.Data;

public interface NetworkService {
  void enableSwitchPort(String switchName, int port);

  void disableSwitchPort(String switchName, int port);

  boolean isSwitchPortEnabled(String switchName, int port);

  void kickPppoeUser(String master, String login);

  boolean isIpReachable(String ip);

  void enableService(long serviceId);

  void disableService(long serviceId);

  Data fetchDhcpWirelessConnection(String master, String mac);

  void enableDhcpWirelessAddress(String master, String address);

  void disableDhcpWirelessAddress(String master, String address);
}
