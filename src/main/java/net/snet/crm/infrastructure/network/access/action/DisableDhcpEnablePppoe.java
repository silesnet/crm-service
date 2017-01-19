package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Dhcp;
import net.snet.crm.infrastructure.network.access.support.DhcpFactory;
import net.snet.crm.infrastructure.network.access.support.Pppoe;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class DisableDhcpEnablePppoe extends BaseAction
{
  private Dhcp originalDhcp;
  private Pppoe pppoe;

  public DisableDhcpEnablePppoe(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize()
  {
    originalDhcp = new DhcpFactory(networkRepository).dhcpOf(serviceId);
    pppoe = new PppoeFactory(networkRepository).pppoeOf(draft);
    return originalDhcp.isValid() || pppoe.isValid();
  }

  @Override
  void updateDatabase()
  {
    if (originalDhcp.isValid()) {
      networkRepository.disableDhcp(
          originalDhcp.switchId(),
          originalDhcp.port(),
          handle
      );
    }
    if (pppoe.isValid()) {
      networkRepository.addPppoe(
          serviceId,
          pppoe.record(),
          handle
      );
    }
  }

  @Override
  void updateNetwork()
  {
    if (originalDhcp.isValid()) {
      networkService.disableSwitchPort(
          originalDhcp.switchName(),
          originalDhcp.port()
      );
      appendMessage(
          "info: closed DHCP switch/port of '%s/%s'",
          originalDhcp.switchName(),
          originalDhcp.port()
      );
    }
  }
}
