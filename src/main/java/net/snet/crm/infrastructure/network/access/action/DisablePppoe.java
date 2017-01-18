package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Pppoe;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class DisablePppoe extends BaseAction
{
  private Pppoe originalPppoe;

  public DisablePppoe(NetworkRepository networkRepository, NetworkService networkService) {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize() {
    originalPppoe = new PppoeFactory(networkRepository).pppoeOf(serviceId);
    return originalPppoe != Pppoe.NULL;
  }

  @Override
  void updateDatabase() {
    networkRepository.removePppoe(serviceId, handle);
    log.info("removed PPPoE for service '{}'", serviceId);
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
  }
}
