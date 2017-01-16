package net.snet.crm.infrastructure.network.access;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.action.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionFactory
{
  private final static Logger log = LoggerFactory.getLogger(ActionFactory.class);
  private final NetworkRepository networkRepository;
  private final NetworkService networkService;

  public ActionFactory(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    this.networkRepository = networkRepository;
    this.networkService = networkService;
  }

  public Action actionOf(final Transitions transition) {
    log.debug("providing action for '{}'", transition);
    switch (transition) {
      case NULL:
        return NoAction.INSTANCE;
      case NoneToPppoe:
      case StaticToPppoe:
        return new EnablePppoe(networkRepository, networkService);
      case NoneToDhcp:
      case StaticToDhcp:
        return new EnableDhcp(networkRepository, networkService);
      case NoneToDhcpWireless:
      case StaticToDhcpWireless:
        return new EnableDhcpWireless(networkRepository, networkService);
      case PppoeToPppoe:
        return new UpdatePppoe(networkRepository, networkService);
      case PppoeToDhcp:
        return new DisablePppoeEnableDhcp(networkRepository, networkService);
      case PppoeToDhcpWireless:
        return new DisablePppoeEnableDhcpWireless(networkRepository, networkService);
      case PppoeToStatic:
      case PppoeToNone:
        return new DisablePppoe(networkRepository, networkService);
      case DhcpToPppoe:
        return new DisableDhcpEnablePppoe(networkRepository, networkService);
      case DhcpToDhcp:
        return new UpdateDhcp(networkRepository, networkService);
      case DhcpToDhcpWireless:
        return new DisableDhcpEnableDhcpWireless(networkRepository, networkService);
      case DhcpToStatic:
      case DhcpToNone:
        return new DisableDhcp(networkRepository, networkService);
      case DhcpWirelessToPppoe:
        return new DisableDhcpWirelessEnablePppoe(networkRepository, networkService);
      case DhcpWirelessToDhcp:
        return new DisableDhcpWirelessEnableDhcp(networkRepository, networkService);
      case DhcpWirelessToDhcpWireless:
        return new UpdateDhcpWireless(networkRepository, networkService);
      case DhcpWirelessToStatic:
      case DhcpWirelessToNone:
        return new DisableDhcpWireless(networkRepository, networkService);
      default:
        throw new IllegalArgumentException(
          String.format("unknown transition '%s'", transition)
        );
    }
  }
}
