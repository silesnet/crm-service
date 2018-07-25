package net.snet.crm.infrastructure.network.access;

import net.snet.crm.domain.shared.data.Data;

import static net.snet.crm.infrastructure.network.access.Events.*;
import static net.snet.crm.infrastructure.network.access.States.*;

public class Access
{
  private final Data draft;
  private final States state;
  private final Events event;

  public Access(Data draft) {
    this.draft = draft;
    this.state = resolveState();
    this.event = resolveEvent();
  }

  public States state() {
    return state;
  }

  public Events event() {
    return event;
  }

  private States resolveState() {
    if (draft.optStringOf("data.product_name", "").length() == 0) {
      return NoneCreated;
    }
    if ("none".equals(draft.optStringOf("data.product_channel", ""))) {
      return None;
    }
    if (draft.optIntOf("data.config", -1) == 2) {
      return Static;
    }
    final int authType = draft.optIntOf("data.auth_type", -1);
    if (authType == 2) {
      return Pppoe;
    }
    if (authType == 1) {
      return "wireless".equals(draft.optStringOf("data.product_channel", ""))
          ? DhcpWireless
          : Dhcp;
    }
    return None;
  }

  private Events resolveEvent() {
    switch (state) {
      case NoneCreated:
        return Created;
      case Pppoe:
        return PppoeConfigured;
      case Dhcp:
        return DhcpConfigured;
      case DhcpWireless:
        return DhcpWirelessConfigured;
      case Static:
        return StaticConfigured;
      case None:
        return Deleted;
    }
    return Deleted;
  }

}
