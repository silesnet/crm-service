package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.DhcpWireless;
import net.snet.crm.infrastructure.network.access.support.DhcpWirelessFactory;
import net.snet.crm.infrastructure.network.access.support.Pppoe;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class DisableDhcpWirelessEnablePppoe extends BaseAction
{
  private DhcpWireless originalDhcpWireless;
  private Pppoe pppoe;

  public DisableDhcpWirelessEnablePppoe(
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
    pppoe = new PppoeFactory(networkRepository).pppoeOf(draft);
    return originalDhcpWireless != DhcpWireless.NULL && pppoe != Pppoe.NULL;
  }

  @Override
  void updateDatabase()
  {
    networkRepository.removeDhcpWireless(serviceId);
    log.info("disabled DHCP Wireless service '{}'", serviceId);

    networkRepository.addPppoe(
        serviceId,
        pppoe.record(),
        handle
    );
    log.info("added PPPoE for service '{}'", serviceId);
  }

}
