package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class EnablePppoe extends BaseAction
{

  private Data pppoe;

  public EnablePppoe(
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
    networkRepository.addPppoe(serviceId, pppoe, handle);
    log.info("added PPPoE for service '{}'", serviceId);
  }

}
