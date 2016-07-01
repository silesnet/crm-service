package net.snet.crm.domain.model.network;

public interface NetworkService {
  void enableSwitchPort(String switchName, int port);

  void disableSwitchPort(String switchName, int port);

  boolean isSwitchPortEnabled(String switchName, int port);

  void kickPppoeUser(String master, String login);

  boolean isIpReachable(String ip);
}
