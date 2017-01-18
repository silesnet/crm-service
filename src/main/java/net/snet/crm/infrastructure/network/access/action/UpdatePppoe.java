package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.data.Data;

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
    return super.initialize();
  }

  @Override
  void updateDatabase() {
    super.updateDatabase();
  }

  @Override
  void updateNetwork() {
    super.updateNetwork();
  }
}
