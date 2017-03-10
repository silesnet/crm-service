package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.support.Pppoe;
import net.snet.crm.infrastructure.network.access.support.PppoeFactory;

public class UpdatePppoe extends BaseAction
{
  private Pppoe originalPppoe;
  private Pppoe pppoe;

  public UpdatePppoe(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  boolean initialize()
  {
    final PppoeFactory factory = new PppoeFactory(networkRepository);
    originalPppoe = factory.pppoeOf(serviceId);
    pppoe = factory.pppoeOf(draft);
    return !originalPppoe.equals(pppoe);
  }

  @Override
  void updateDatabase()
  {
    if (originalPppoe.isValid() && pppoe.isValid())
    {
      networkRepository.updatePppoe(
          serviceId,
          pppoe.record(),
          handle
      );
    } else if (originalPppoe.isNotValid() && pppoe.isValid())
    {
      networkRepository.addPppoe(
          serviceId,
          pppoe.record(),
          handle
      );
    } else
    {
      networkRepository.removePppoe(serviceId);
    }
  }

  @Override
  void updateNetwork()
  {
    if (originalPppoe.isValid())
    {
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
}
