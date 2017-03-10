package net.snet.crm.infrastructure.network.access.support;

import net.snet.crm.domain.shared.data.Data;

public final class DhcpWireless
{
  public static final DhcpWireless NULL = new DhcpWireless(null);

  private final Data record;

  public static DhcpWireless of(Data record) {
    if (record == null || record.isEmpty())
    {
      return NULL;
    }
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

    DhcpWireless that = (DhcpWireless) o;

    return record != null ? record.equals(that.record) : that.record == null;
  }

  @Override
  public int hashCode()
  {
    return record != null ? record.hashCode() : 0;
  }

  @Override
  public String toString()
  {
    return "DhcpWireless{" +
        "record=" + record +
        '}';
  }
}
