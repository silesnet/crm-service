package net.snet.crm.domain.shared.command;

public enum Commands {
  DISCONNECT("disconnect"),
  RECONNECT("reconnect")
  ;
  private final String name;

  Commands(String name) {
    this.name = name;
  }

  public String command() {
    return this.name;
  }

  public static Commands of(String name) {
    return Commands.valueOf(name.toUpperCase());
  }
}
