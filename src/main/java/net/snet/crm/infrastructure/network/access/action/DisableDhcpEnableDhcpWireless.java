package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Dhcp;
import net.snet.crm.infrastructure.network.access.support.DhcpFactory;
import net.snet.crm.infrastructure.network.access.support.DhcpWireless;
import net.snet.crm.infrastructure.network.access.support.DhcpWirelessFactory;

public class DisableDhcpEnableDhcpWireless extends BaseAction
{
  private Dhcp originalDhcp;
  private DhcpWireless dhcpWireless;

  public DisableDhcpEnableDhcpWireless(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize() {
    originalDhcp = new DhcpFactory(networkRepository).dhcpOf(serviceId);
    dhcpWireless = new DhcpWirelessFactory(networkRepository).dhcpWirelessOf(draft);
    return originalDhcp != Dhcp.NULL && dhcpWireless != DhcpWireless.NULL;
  }

  @Override
  void updateDatabase() {
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
    networkRepository.addDhcpWireless(
        serviceId,
        dhcpWireless.record()
    );
    log.info("enabled DHCP Wireless service '{}'", serviceId);
  }

  @Override
  void updateNetwork() {
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
