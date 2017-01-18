package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Pppoe;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class EnablePppoe extends BaseAction
{
  private Pppoe pppoe;

  public EnablePppoe(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize()
  {
    pppoe = new PppoeFactory(networkRepository).pppoeOf(draft);
    return pppoe != Pppoe.NULL;
  }

  @Override
  void updateDatabase()
  {
    networkRepository.addPppoe(
        serviceId,
        pppoe.record(),
        handle
    );
    log.info("added PPPoE for service '{}'", serviceId);
  }

}
