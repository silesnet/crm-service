package net.snet.crm.infrastructure.network.access.support;

import com.google.common.collect.ImmutableSet;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class DhcpWireless
{
  public static final DhcpWireless NULL = new DhcpWireless(null);
  private static Set<String> DHCP_WIRELESS_PROPS = ImmutableSet.of(
      "mac", "ip", "ip_class", "interface", "master"
  );
  private final Data record;

  public static DhcpWireless of(Data record) {
    if (record == null || record.isEmpty())
    {
      return NULL;
    }
    return new DhcpWireless(stripUnrelatedKeysOf(record));
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

  private static Data stripUnrelatedKeysOf(Data record) {
    final Map<String, Object> result = new LinkedHashMap<>(record.asMap());
    result.keySet().retainAll(DHCP_WIRELESS_PROPS);
    return MapData.of(result);
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
