package net.snet.crm.infrastructure.network.access;

import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

import static net.snet.crm.infrastructure.network.access.Events.*;
import static net.snet.crm.infrastructure.network.access.States.*;

public class ServiceDraft
{
  private final Draft draft;
  private final Data data;
  private final States state;
  private final Events event;

  public ServiceDraft(Draft draft) {
    if (Draft.Entity.SERVICES != draft.entity()) {
      throw new IllegalArgumentException("expected services draft but got " + draft.entity());
    }
    this.draft = draft;
    this.data = MapData.of(draft.data());
    this.state = resolveState();
    this.event = resolveEvent();
  }

  public long serviceId() {
    return draft.entityId();
  }

  public States state() {
    return state;
  }

  public Events event() {
    return event;
  }

  private States resolveState() {
    if (data.optionalIntOf("config", -1) == 2) {
      return Static;
    }
    final int authType = data.optionalIntOf("auth_type", -1);
    if (authType == 2) {
      return Pppoe;
    }
    if (authType == 1) {
      return "wireless".equals(data.optionalStringOf("product_channel", ""))
          ? DhcpWireless
          : Dhcp;
    }
    return None;
  }

  private Events resolveEvent() {
    switch (state) {
      case None: return Created;
      case Pppoe: return PppoeConfigured;
      case Dhcp: return DhcpConfigured;
      case DhcpWireless: return DhcpWirelessConfigured;
      case Static: return StaticConfigured;
    }
    return Deleted;
  }

}
