package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.DhcpWireless;
import net.snet.crm.infrastructure.network.access.support.DhcpWirelessFactory;

public class EnableDhcpWireless extends BaseAction
{

  private DhcpWireless dhcpWireless;

  public EnableDhcpWireless(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize() {
    dhcpWireless =
        new DhcpWirelessFactory(networkRepository).dhcpWirelessOf(draft);
    return dhcpWireless != DhcpWireless.NULL;
  }

  @Override
  void updateDatabase() {
    networkRepository.addDhcpWireless(serviceId, dhcpWireless.record());
    log.info("enabled DHCP Wireless service '{}'", serviceId);
  }
}
