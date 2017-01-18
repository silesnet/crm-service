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
    return originalPppoe != Pppoe.NULL && dhcp != Dhcp.NULL;
  }

  @Override
  void updateDatabase() {
    networkRepository.removePppoe(serviceId, handle);
    log.info("removed PPPoE for service '{}'", serviceId);

    networkRepository.bindDhcp(
        serviceId,
        dhcp.switchId(),
        dhcp.port(),
        handle
    );
    log.info(
        "enabled DHCP switch port '{}/{} for service '{}'",
        dhcp.switchName(),
        dhcp.port(),
        serviceId
    );
  }

  @Override
  void updateNetwork() {
    networkService.kickPppoeUser(
        originalPppoe.master(),
        originalPppoe.login()
    );
    appendMessage(
        "info: kicked '%s' from '%s'",
        originalPppoe.login(),
        originalPppoe.master()
    );

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
