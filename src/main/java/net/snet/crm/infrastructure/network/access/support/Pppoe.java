package net.snet.crm.infrastructure.network.access.support;

import com.google.common.collect.ImmutableSet;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class Pppoe
{
  public static Pppoe NULL = new Pppoe(null, null, null);
  private static Set<String> PPPOE_PROPS = ImmutableSet.of(
      "login", "password", "mac", "mode", "ip", "ip_class", "interface", "master"
  );

  private final Data record;
  private final String master;
  private final String login;

  public static Pppoe of(Data record) {
    return new Pppoe(
        stripUnrelatedKeysOf(record),
        record.stringOf("master"),
        record.stringOf("login")
    );
  }

  private Pppoe(Data record, String master, String login) {
    this.record = record;
    this.master = master;
    this.login = login;
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

  public String master() {
    return master;
  }

  public String login() {
    return login;
  }

  private static Data stripUnrelatedKeysOf(Data record) {
    final Map<String, Object> result = new LinkedHashMap<>(record.asMap());
    result.keySet().retainAll(PPPOE_PROPS);
    return MapData.of(result);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof Pppoe))
    {
      return false;
    }

    Pppoe pppoe = (Pppoe) o;

    if (record != null ? !record.equals(pppoe.record) : pppoe.record != null)
    {
      return false;
    }
    if (master != null ? !master.equals(pppoe.master) : pppoe.master != null)
    {
      return false;
    }
    return login != null ? login.equals(pppoe.login) : pppoe.login == null;
  }

  @Override
  public int hashCode()
  {
    int result = record != null ? record.hashCode() : 0;
    result = 31 * result + (master != null ? master.hashCode() : 0);
    result = 31 * result + (login != null ? login.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return "Pppoe{" +
        "record=" + record +
        '}';
  }
}
