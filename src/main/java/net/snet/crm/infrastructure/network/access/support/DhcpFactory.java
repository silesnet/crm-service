package net.snet.crm.infrastructure.network.access.support;

import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

public class DhcpFactory
{
  private final NetworkRepository networkRepository;

  public DhcpFactory(NetworkRepository networkRepository) {
    this.networkRepository = networkRepository;
  }

  public Dhcp dhcpOf(Data draft)
  {
    final Data data = draft.optDataOf("data");
    if (data.optIntOf("auth_a", -1) == -1 ||
        data.optIntOf("auth_b", -1) == -1) {
      return Dhcp.NULL;
    }
    return Dhcp.of(
        data.intOf("auth_a"),
        data.intOf("auth_b"),
        switchOf(data.intOf("auth_a"))
    );
  }

  public Dhcp dhcpOf(long serviceId) {
    final Data data = MapData.of(networkRepository.findServiceDhcp(serviceId));
    if (data.isEmpty()) {
      return Dhcp.NULL;
    }
    return Dhcp.of(
        data.intOf("network_id"),
        data.intOf("port"),
        data.stringOf("switch")
    );
  }

  private String switchOf(int switchId) {
    return MapData.of(networkRepository.findDevice(switchId))
                  .optStringOf("name");
  }

}
