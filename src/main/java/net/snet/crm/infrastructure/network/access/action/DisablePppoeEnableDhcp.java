package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Dhcp;
import net.snet.crm.infrastructure.network.access.support.DhcpFactory;
import net.snet.crm.infrastructure.network.access.support.Pppoe;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class DisablePppoeEnableDhcp extends BaseAction
{
  private Pppoe originalPppoe;
  private Dhcp dhcp;

  public DisablePppoeEnableDhcp(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize()
  {
    originalPppoe = new PppoeFactory(networkRepository).pppoeOf(serviceId);
    dhcp = new DhcpFactory(networkRepository).dhcpOf(draft);
    return originalPppoe.isValid() || dhcp.isValid();
  }

  @Override
  void updateDatabase()
  {
    if (originalPppoe.isValid()) {
      networkRepository.removePppoe(serviceId, handle);
    }
    if (dhcp.isValid()) {
      networkRepository.bindDhcp(
          serviceId,
          dhcp.switchId(),
          dhcp.port(),
          handle
      );
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
    if (dhcp.isValid()) {
      networkService.enableSwitchPort(
          dhcp.switchName(),
          dhcp.port()
      );
      appendMessage(
          "info: opened DHCP switch port of '%s/%s'",
          dhcp.switchName(),
          dhcp.port()
      );
    }
  }
}
