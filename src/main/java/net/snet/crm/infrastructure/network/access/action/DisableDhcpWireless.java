package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.DhcpWireless;
import net.snet.crm.infrastructure.network.access.support.DhcpWirelessFactory;

public class DisableDhcpWireless extends BaseAction
{
  private DhcpWireless originalDhcpWireless;

  public DisableDhcpWireless(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize()
  {
    originalDhcpWireless =
        new DhcpWirelessFactory(networkRepository).dhcpWirelessOf(serviceId);
    return originalDhcpWireless.isValid();
  }

  @Override
  void updateDatabase()
  {
    networkRepository.removeDhcpWireless(serviceId);
  }
}
