package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.DhcpWireless;
import net.snet.crm.infrastructure.network.access.support.DhcpWirelessFactory;
import net.snet.crm.infrastructure.network.access.support.Pppoe;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class DisablePppoeEnableDhcpWireless extends BaseAction
{
  private Pppoe originalPppoe;
  private DhcpWireless dhcpWireless;

  public DisablePppoeEnableDhcpWireless(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize()
  {
    originalPppoe = new PppoeFactory(networkRepository).pppoeOf(serviceId);
    dhcpWireless =
        new DhcpWirelessFactory(networkRepository).dhcpWirelessOf(draft);
    return originalPppoe.isValid() || dhcpWireless.isValid();
  }

  @Override
  void updateDatabase()
  {
    if (originalPppoe.isValid()) {
      networkRepository.removePppoe(serviceId, handle);
    }
    if (dhcpWireless.isValid()) {
      networkRepository.addDhcpWireless(serviceId, dhcpWireless.record());
    }
  }

  @Override
  void updateNetwork()
  {
    if (originalPppoe.isValid()) {
      networkService.kickPppoeUser(
          originalPppoe.master(),
          originalPppoe.login()
      );
      appendMessage(
          "info: kicked '%s' from '%s'",
          originalPppoe.login(),
          originalPppoe.master()
      );
    }
  }
}
