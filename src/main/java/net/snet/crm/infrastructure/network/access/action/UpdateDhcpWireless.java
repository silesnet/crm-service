package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.DhcpWireless;
import net.snet.crm.infrastructure.network.access.support.DhcpWirelessFactory;

public class UpdateDhcpWireless extends BaseAction
{
  private DhcpWireless originalDhcpWireless;
  private DhcpWireless dhcpWireless;

  public UpdateDhcpWireless(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize() {
    final DhcpWirelessFactory factory = new DhcpWirelessFactory(networkRepository);
    originalDhcpWireless = factory.dhcpWirelessOf(serviceId);
    dhcpWireless = factory.dhcpWirelessOf(draft);
    return originalDhcpWireless != DhcpWireless.NULL &&
        dhcpWireless != DhcpWireless.NULL;
  }

  @Override
  void updateDatabase() {
    networkRepository.updateDhcpWireless(serviceId, dhcpWireless.record());
    log.info("updated DHCP Wireless service '{}'", serviceId);
  }
}
