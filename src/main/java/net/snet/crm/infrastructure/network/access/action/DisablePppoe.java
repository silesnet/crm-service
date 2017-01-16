package net.snet.crm.infrastructure.network.access.action;

import com.google.common.collect.Lists;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import org.skife.jdbi.v2.Handle;

import java.util.List;

public class DisablePppoe extends BaseAction
{
  private Data pppoe;

  public DisablePppoe(
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    super(networkRepository, networkService);
  }

  @Override
  void updateDatabase(long serviceId, Handle handle) {
    pppoe = MapData.of(networkRepository.findServicePppoe(serviceId));
    networkRepository.removePppoe(serviceId, handle);
  }

  @Override
  List<String> updateNetwork(long serviceId) {
    final String master = pppoe.stringOf("master");
    final String login = pppoe.stringOf("login");
    networkService.kickPppoeUser(master, login);
    return Lists.newArrayList(
        String.format("info: kicked '%s' from '%s'", login, master));
  }
}
