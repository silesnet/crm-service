package net.snet.crm.infrastructure.network.access.support;

import net.snet.crm.domain.shared.data.Data;

public class DhcpWireless
{
  public static final DhcpWireless NULL = new DhcpWireless(null);

  private final Data record;

  public static DhcpWireless of(Data record) {
    return new DhcpWireless(record);
  }

  private DhcpWireless(Data record) {
    this.record = record;
  }

  public boolean isValid() {
    return this != NULL;
  }

  public boolean isNotValid() {
    return this == NULL;
  }

  public Data record() {
    return record;
  }
}
