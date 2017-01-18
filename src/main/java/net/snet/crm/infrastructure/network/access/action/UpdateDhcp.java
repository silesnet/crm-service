package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Dhcp;
import net.snet.crm.infrastructure.network.access.support.DhcpFactory;

public class UpdateDhcp extends BaseAction
{
  private Dhcp originalDhcp;
  private Dhcp dhcp;

  public UpdateDhcp(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize()
  {
    final DhcpFactory factory = new DhcpFactory(networkRepository);
    originalDhcp = factory.dhcpOf(serviceId);
    dhcp = factory.dhcpOf(draft);
    return originalDhcp != Dhcp.NULL && dhcp != Dhcp.NULL;
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
    networkService.disableSwitchPort(
        originalDhcp.switchName(),
        originalDhcp.port()
    );
    appendMessage(
        "info: closed DHCP switch/port of '%s/%s'",
        originalDhcp.switchName(),
        originalDhcp.port()
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
