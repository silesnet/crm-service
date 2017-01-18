package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class UpdatePppoe extends BaseAction
{
  private Data pppoe;

  public UpdatePppoe(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize() {
    final PppoeFactory factory = new PppoeFactory(networkRepository);
    pppoe = factory.pppoeOf(draft);
    return !pppoe.isEmpty();
  }

  @Override
  void updateDatabase() {
    networkRepository.updatePppoe(serviceId, pppoe, handle);
    log.info("updated PPPoE for service '{}'", serviceId);
  }

  @Override
  void updateNetwork() {
    final String master = pppoe.stringOf("master");
    final String login = pppoe.stringOf("login");
    networkService.kickPppoeUser(master, login);
    appendMessage("info: kicked '%s' from '%s'", login, master);
  }
}
