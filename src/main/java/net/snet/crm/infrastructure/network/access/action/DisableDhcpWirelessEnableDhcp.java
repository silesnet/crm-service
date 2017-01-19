package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Dhcp;
import net.snet.crm.infrastructure.network.access.support.DhcpFactory;
import net.snet.crm.infrastructure.network.access.support.DhcpWireless;
import net.snet.crm.infrastructure.network.access.support.DhcpWirelessFactory;

public class DisableDhcpWirelessEnableDhcp extends BaseAction
{
  private DhcpWireless originalDhcpWireless;
  private Dhcp dhcp;

  public DisableDhcpWirelessEnableDhcp(
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
    dhcp = new DhcpFactory(networkRepository).dhcpOf(draft);
    return originalDhcpWireless.isValid() || dhcp.isValid();
  }

  @Override
  void updateDatabase()
  {
    if (originalDhcpWireless.isValid()) {
      networkRepository.removeDhcpWireless(serviceId);
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
