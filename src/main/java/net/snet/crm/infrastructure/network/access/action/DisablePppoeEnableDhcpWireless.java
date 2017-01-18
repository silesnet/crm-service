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
    return originalPppoe != Pppoe.NULL && dhcpWireless != DhcpWireless.NULL;
  }

  @Override
  void updateDatabase()
  {
    networkRepository.removePppoe(serviceId, handle);
    log.info("removed PPPoE for service '{}'", serviceId);

    networkRepository.addDhcpWireless(serviceId, dhcpWireless.record());
    log.info("enabled DHCP Wireless service '{}'", serviceId);
  }

  @Override
  void updateNetwork()
  {
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
