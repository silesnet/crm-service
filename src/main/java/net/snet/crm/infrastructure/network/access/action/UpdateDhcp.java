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
    return !originalDhcp.equals(dhcp);
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
