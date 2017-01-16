package net.snet.crm.infrastructure.network.access.action;

import com.google.common.collect.Lists;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.infrastructure.network.access.Action;
import org.skife.jdbi.v2.Handle;

import java.util.ArrayList;
import java.util.List;

public class BaseAction implements Action
{
  final NetworkRepository networkRepository;
  final NetworkService networkService;

  public BaseAction(NetworkRepository networkRepository, NetworkService networkService) {
    this.networkRepository = networkRepository;
    this.networkService = networkService;
  }

  @Override
  public List<String> perform(long serviceId, Handle handle) {
    updateDatabase(serviceId, handle);
    try {
      return updateNetwork(serviceId);
    } catch (Exception e) {
      return Lists.newArrayList(
          String.format("warning: %s", e.getMessage())
      );
    }
  }

  void updateDatabase(long serviceId, Handle handle) {
  }

  List<String> updateNetwork(long serviceId) {
    return new ArrayList<>();
  }

}
