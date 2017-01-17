package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

public class DisablePppoe extends BaseAction
{
  private Data pppoe;

  public DisablePppoe(NetworkRepository networkRepository, NetworkService networkService) {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize() {
    pppoe = MapData.of(networkRepository.findServicePppoe(serviceId));
    return !pppoe.isEmpty();
  }

  @Override
  void updateDatabase() {
    networkRepository.removePppoe(serviceId, handle);
    log.info("removed PPPoE for service '{}'", serviceId);
  }

  @Override
  void updateNetwork() {
    final String master = pppoe.stringOf("master");
    final String login = pppoe.stringOf("login");
    networkService.kickPppoeUser(master, login);
    appendMessage("info: kicked '%s' from '%s'", login, master);
  }
}
