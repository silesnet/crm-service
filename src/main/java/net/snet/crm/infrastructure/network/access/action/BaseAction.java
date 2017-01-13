package net.snet.crm.infrastructure.network.access.action;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.Action;
import org.skife.jdbi.v2.Handle;

public class BaseAction implements Action
{
  private final NetworkRepository networkRepository;
  private final NetworkService networkService;

  public BaseAction(NetworkRepository networkRepository, NetworkService networkService) {
    this.networkRepository = networkRepository;
    this.networkService = networkService;
  }

  @Override
  public void perform(Handle handle) {
    updateDatabase(handle);
    updateNetwork();
  }

  void updateDatabase(Handle handle) {
  }

  void updateNetwork() {
  }

}
