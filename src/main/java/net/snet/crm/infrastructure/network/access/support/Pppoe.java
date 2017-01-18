package net.snet.crm.infrastructure.network.access.support;

import net.snet.crm.domain.shared.data.Data;

public class Pppoe
{
  public static Pppoe NULL = new Pppoe(null, null, null);

  private final Data record;
  private final String master;
  private final String login;

  public static Pppoe of(Data record) {
    return new Pppoe(
        record,
        record.stringOf("master"),
        record.stringOf("login")
    );
  }

  private Pppoe(Data record, String master, String login) {
    this.record = record;
    this.master = master;
    this.login = login;
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
}
