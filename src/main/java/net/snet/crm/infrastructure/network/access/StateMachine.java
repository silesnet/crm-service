package net.snet.crm.infrastructure.network.access;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.snet.crm.infrastructure.network.access.States.*;
import static net.snet.crm.infrastructure.network.access.Transitions.*;

public class StateMachine
{
  private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

  public Transitions transitionOf(States state, Events event) {
    log.debug("resolving transition for '{}.{}'", state, event);
    if (None == state) {
      switch (event) {
        case Created: return NULL;
        case PppoeConfigured: return NoneToPppoe;
        case DhcpConfigured: return NoneToDhcp;
        case DhcpWirelessConfigured: return NoneToDhcpWireless;
        case StaticConfigured: return NULL;
        case Deleted: return NULL;
      }
    } else if (Pppoe == state) {
      switch (event) {
        case Created: return NULL;
        case PppoeConfigured: return PppoeToPppoe;
        case DhcpConfigured: return PppoeToDhcp;
        case DhcpWirelessConfigured: return PppoeToDhcpWireless;
        case StaticConfigured: return PppoeToStatic;
        case Deleted: return PppoeToNone;
      }
    } else if (Dhcp == state) {
      switch (event) {
        case Created: return NULL;
        case PppoeConfigured: return DhcpToPppoe;
        case DhcpConfigured: return DhcpToDhcp;
        case DhcpWirelessConfigured: return DhcpToDhcpWireless;
        case StaticConfigured: return DhcpToStatic;
        case Deleted: return DhcpToNone;
      }
    } else if (DhcpWireless == state) {
      switch (event) {
        case Created: return NULL;
        case PppoeConfigured: return DhcpWirelessToPppoe;
        case DhcpConfigured: return DhcpWirelessToDhcp;
        case DhcpWirelessConfigured: return DhcpWirelessToDhcpWireless;
        case StaticConfigured: return DhcpWirelessToStatic;
        case Deleted: return DhcpWirelessToNone;
      }
    } else if (Static == state) {
      switch (event) {
        case Created: return NULL;
        case PppoeConfigured: return StaticToPppoe;
        case DhcpConfigured: return StaticToDhcp;
        case DhcpWirelessConfigured: return StaticToDhcpWireless;
        case StaticConfigured: return NULL;
        case Deleted: return NULL;
      }
    }
    throw new IllegalArgumentException(
      String.format("unknown state event combination: '%s', '%s'", state, event)
    );
  }
}
