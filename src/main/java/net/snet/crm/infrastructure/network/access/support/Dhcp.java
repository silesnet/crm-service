package net.snet.crm.infrastructure.network.access.support;

import net.snet.crm.domain.shared.data.Data;

public final class Dhcp
{
  public static Dhcp NULL = new Dhcp();

  private final int switchId;
  private final int port;
  private final String switchName;

  public static Dhcp of(int switchId, int port, String switchName) {
    if (switchId < 0 ||
        port < 0 ||
        switchName == null ||
        switchName.trim().length() == 0) {
      return NULL;
    }
    return new Dhcp(switchId, port, switchName);
  }

  public static Dhcp of(Data data) {
    return of(
      data.optIntOf("switchId", -1),
      data.optIntOf("port", -1),
      data.optStringOf("switch")
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

  public boolean isValid() {
    return this != NULL;
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

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    Dhcp dhcp = (Dhcp) o;

    if (switchId != dhcp.switchId)
    {
      return false;
    }
    if (port != dhcp.port)
    {
      return false;
    }
    return switchName != null ? switchName.equals(dhcp.switchName) : dhcp.switchName == null;
  }

  @Override
  public int hashCode()
  {
    int result = switchId;
    result = 31 * result + port;
    result = 31 * result + (switchName != null ? switchName.hashCode() : 0);
    return result;
  }
}

