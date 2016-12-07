package net.snet.crm.domain.shared.event;

public enum Events {
  DISCONNECTED("disconnected"),
  RECONNECTED("reconnected"),
  CONNECTED("connected")
  ;
  private final String name;

  Events(String name) {
    this.name = name;
  }

  public static Events of(String name) {
    return Events.valueOf(name.toUpperCase());
  }

  public String event() {
    return name;
  }
}
