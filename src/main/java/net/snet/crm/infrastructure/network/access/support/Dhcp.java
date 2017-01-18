package net.snet.crm.infrastructure.network.access.support;

import net.snet.crm.domain.shared.data.Data;

public class Dhcp
{
  public static Dhcp NULL = new Dhcp();

  private final int switchId;
  private final int port;
  private final String switchName;

  public static Dhcp of(int switchId, int port, String switchName) {
    if (switchId == -1 ||
        port == -1 ||
        switchName == null ||
        switchName.length() == 0) {
      return NULL;
    }
    return new Dhcp(switchId, port, switchName);
  }

  public static Dhcp of(Data data) {
    return of(
      data.intOf("switchId"),
      data.intOf("port"),
      data.stringOf("switch")
    );
  }

  private Dhcp() {
    switchId = -1;
    port = -1;
    switchName = "";
  }

  private Dhcp(int switchId, int port, String switchName) {
    this.switchId = switchId;
    this.port = port;
    this.switchName = switchName;
  }

  public int switchId() {
    return switchId;
  }

  public int port() {
    return port;
  }

  public String switchName() {
    return switchName;
  }
}

