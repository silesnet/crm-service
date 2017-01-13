package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;

public class DisableDhcpEnableDhcpWireless extends BaseAction
{
  public DisableDhcpEnableDhcpWireless(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }
}
