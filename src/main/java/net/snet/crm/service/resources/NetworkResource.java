package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkRepository.Country;
import net.snet.crm.domain.model.network.NetworkRepository.DeviceType;
import net.snet.crm.domain.model.network.NetworkService;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import net.snet.crm.infrastructure.persistence.jdbi.DbiNetworkRepository;
import net.snet.crm.service.bo.Network;
import net.snet.crm.service.dao.NetworkDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.service.utils.Entities.*;

@Path("/networks")
public class NetworkResource
{

  private static final Logger logger = LoggerFactory.getLogger(NetworkResource.class);

  private NetworkDAO networkDAO;
  private final NetworkRepository networkRepository;
  private final NetworkService networkService;

  public NetworkResource(DBI dbi, NetworkService networkService) {
    this.networkDAO = dbi.onDemand(NetworkDAO.class);
    this.networkRepository = new DbiNetworkRepository(dbi);
    this.networkService = networkService;
  }

  @PUT
  @Path("pppoe/{login}/kick/{master}")
  @Produces({"application/json; charset=UTF-8"})
  public Response kickPppoeUser(
      @PathParam("login") String login, @PathParam("master") String master)
  {
    networkService.kickPppoeUser(master, login);
    return Response.ok(ImmutableMap.of()).build();
  }

  @GET
  @Path("dhcp-wireless/{serviceId}/connection")
  @Produces({"application/json; charset=UTF-8"})
  public Response dhcpWirelessLastIpOf(@PathParam("serviceId") long serviceId) {
    final Map<String, Object> result;
    final Data dhcpWireless = networkRepository.findServiceDhcpWireless(serviceId);
    if (dhcpWireless.hasValue("master") && dhcpWireless.hasValue("mac.value"))
    {
      final String master = dhcpWireless.stringOf("master");
      final String mac = dhcpWireless.stringOf("mac.value").toUpperCase();
      final Data connection = networkService.fetchDhcpWirelessConnection(master, mac);
      result = connection.asMap();
    }
    else {
      result = ImmutableMap.of();
    }
    return Response.ok(ImmutableMap.of("connection", result)).build();
  }

  @GET
  @Path("pppoe/{login}/last-ip")
  @Produces({"application/json; charset=UTF-8"})
  public Response pppoeLastIpOf(@PathParam("login") String login) {
    return Response.ok(ImmutableMap.of("lastIp",
                                       networkRepository.findPppoeUserLastIp(login))).build();
  }

  @PUT
  @Path("dhcp-wireless/{serviceId}")
  @Produces({"application/json; charset=UTF-8"})
  public Response updateServiceWirelessDhcp(
      @PathParam("serviceId") long serviceId,
      Data body)
  {
    checkState(body.hasValue("services.dhcp_wireless"),
               "no wireless DHCP update provided");
    final Data update = body.dataOf("services.dhcp_wireless");
    if (update.isEmpty()) {
      networkRepository.removeDhcpWireless(serviceId);
    } else {
      final Data current = networkRepository.findServiceDhcpWireless(serviceId);
      if (current.isEmpty()) {
        networkRepository.addDhcpWireless(serviceId, update);
      } else {
        networkRepository.updateDhcpWireless(serviceId, update);
      }
    }
    return Response.ok(MapData.EMPTY).build();
  }

  @PUT
  @Path("pppoe/{serviceId}")
  @Produces({"application/json; charset=UTF-8"})
  public Response updateServicePppoe(
      @PathParam("serviceId") long serviceId,
      Map<String, Object> updateBody)
  {
    ValueMap update = valueMapOf(updateBody);
    Value serviceUpdate = update.get("services");
    checkState(!serviceUpdate.isNull(), "no service update body sent");
    Value pppoeUpdate = serviceUpdate.asMap().get("pppoe");
    if (!pppoeUpdate.isNull()) {
      if (pppoeUpdate.asMap().map().isEmpty()) {
        logger.debug("removing PPPoE for service '{}'", serviceId);
        networkRepository.removePppoe(serviceId);
      } else {
        ValueMap currentPppoe = valueMapOf(networkRepository.findServicePppoe(serviceId));
        if (currentPppoe.map().isEmpty()) {
          logger.debug("adding PPPoE for service '{}'", serviceId);
          final Map<String, Object> pppoe = pppoeUpdate.asMap().map();
          pppoe.put("service_id", serviceId);
          networkRepository.addPppoe(serviceId, pppoe);
        } else {
          logger.debug("updating PPPoE for service '{}'", serviceId);
          networkRepository.updatePppoe(serviceId, pppoeUpdate.asMap().map());
        }
      }
    }
    return Response.ok(ImmutableMap.of()).build();
  }

  @PUT
  @Path("dhcp/{serviceId}")
  @Produces({"application/json; charset=UTF-8"})
  public Response updateServiceDhcp(
      @PathParam("serviceId") long serviceId,
      Map<String, Object> updateBody)
  {
    ValueMap update = valueMapOf(updateBody);
    Value serviceUpdate = update.get("services");
    checkState(!serviceUpdate.isNull(), "no service update body sent");
    Value dhcpUpdate = serviceUpdate.asMap().get("dhcp");
    if (!dhcpUpdate.isNull()) {
      if (dhcpUpdate.asMap().map().isEmpty()) {
        logger.debug("deleting DHCP for service '{}'", serviceId);
        ValueMap currentDhcp = valueMapOf(networkRepository.findServiceDhcp(serviceId));
        final int networkId = currentDhcp.get("network_id").asIntegerOr(-1);
        final int port = currentDhcp.get("port").asIntegerOr(-1);
        checkState(networkId > 0, "switch network_id does not exist for service '%s'", serviceId);
        checkState(port >= 0, "switch port not does not exit for service '%s'", serviceId);
        networkRepository.disableDhcp(networkId, port);
      } else {
        logger.debug("updating DHCP get service '{}'", serviceId);
        final int networkId = dhcpUpdate.asMap().get("network_id").asIntegerOr(-1);
        final int port = dhcpUpdate.asMap().get("port").asIntegerOr(-1);
        checkState(networkId > 0, "new switch network_id not provided");
        checkState(port >= 0, "new switch port not provided");
        networkRepository.bindDhcp(serviceId, networkId, port);
        final Map<String, Object> propsUpdate = dhcpUpdate.asMap().map();
        propsUpdate.put("ip", resolveIpAddress(dhcpUpdate.asMap().get("ip").asStringValueOr("AUTO")));
        propsUpdate.remove("network_id");
        propsUpdate.remove("port");
        if (!propsUpdate.isEmpty()) {
          networkRepository.updateDhcp(serviceId, propsUpdate);
        }
      }
    }
    return Response.ok(ImmutableMap.of()).build();
  }

  private String resolveIpAddress(String ip) {
    if ("AUTO".equals(ip) || InetAddresses.isInetAddress(ip)) {
      return ip;
    }
    throw new IllegalArgumentException("invalid ip address: '" + ip + "'");
  }

  @GET
  @Path("{country}/devices")
  @Produces({"application/json; charset=UTF-8"})
  public Response findDevicesByCountry(
      @PathParam("country") String countryParam,
      @QueryParam("deviceType") String deviceTypeParam)
  {
    final Country country = Country.valueOf(countryParam.toUpperCase());
    final DeviceType deviceType = DeviceType.valueOf(deviceTypeParam.toUpperCase());
    List<Map<String, Object>> devices =
        networkRepository.findDevicesByCountryAndType(country, deviceType);
    return Response.ok(ImmutableMap.of("devices", devices)).build();
  }


  @GET
  @Path("/devices/{deviceId}")
  @Produces({"application/json; charset=UTF-8"})
  public Response findDeviceById(@PathParam("deviceId") int deviceId) {
    return Response.ok(
        ImmutableMap.of("devices", networkRepository.findDevice(deviceId)))
                   .build();
  }

  @GET
  @Path("/masters")
  @Produces({"application/json; charset=UTF-8"})
  public Response allMasters() {
    return Response.ok(ImmutableMap.of("masters", networkRepository.findAllMasters())).build();
  }

  @GET
  @Path("/routers")
  @Produces({"application/json; charset=UTF-8"})
  @Timed(name = "get-requests")
  public Map<String, Object> getAllNetworks() {

    final HashMap<String, Object> coreRoutersMap = new HashMap<>();

    Iterator<Network> routers = networkDAO.allMasters();

    coreRoutersMap.put("core_routers", routers);

    return coreRoutersMap;
  }

  @GET
  @Path("/ssids")
  @Produces({"application/json; charset=UTF-8"})
  @Timed(name = "get-requests")
  public Map<String, Object> getAllSsids() {

    final HashMap<String, Object> networksMap = new HashMap<>();

    Iterator<Network> networks = networkDAO.allSsids();

    networksMap.put("ssids", networks);

    return networksMap;
  }

  @GET
  @Path("/ip/{ipAddress}")
  @Produces({"application/json; charset=UTF-8"})
  public Response isIpReachable(@PathParam("ipAddress") String ipAddress) {
    final ImmutableMap<String, ? extends Serializable> data =
        ImmutableMap.of("ip", ipAddress, "isReachable", networkService.isIpReachable(ipAddress));
    return Response.ok(data).build();
  }

}
