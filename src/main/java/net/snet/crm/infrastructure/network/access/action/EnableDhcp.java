package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Dhcp;
import net.snet.crm.infrastructure.network.access.support.DhcpFactory;

public class EnableDhcp extends BaseAction
{
  private Dhcp dhcp;

  public EnableDhcp(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize()
  {
    dhcp = new DhcpFactory(networkRepository).dhcpOf(draft);
    return dhcp != Dhcp.NULL;
  }

  @Override
  void updateDatabase()
  {
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
  void updateNetwork()
  {
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
