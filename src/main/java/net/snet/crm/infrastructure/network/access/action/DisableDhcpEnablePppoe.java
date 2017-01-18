package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.*;

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
    return originalDhcp != Dhcp.NULL && pppoe != Pppoe.NULL;
  }

  @Override
  void updateDatabase()
  {
    networkRepository.disableDhcp(
        originalDhcp.switchId(),
        originalDhcp.port(),
        handle
    );
    log.info(
        "disabled DHCP switch port '{}/{} for service '{}'",
        originalDhcp.switchName(),
        originalDhcp.port(),
        serviceId
    );
    networkRepository.addPppoe(
        serviceId,
        pppoe.record(),
        handle
    );
    log.info("added PPPoE for service '{}'", serviceId);
  }

  @Override
  void updateNetwork()
  {
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
