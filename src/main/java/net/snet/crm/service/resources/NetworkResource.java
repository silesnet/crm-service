package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkRepository.Country;
import net.snet.crm.domain.model.network.NetworkRepository.DeviceType;
import net.snet.crm.infrastructure.persistence.jdbi.DbiNetworkRepository;
import net.snet.crm.service.bo.Network;
import net.snet.crm.service.dao.NetworkDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.service.utils.Entities.*;

@Path("/networks")
public class NetworkResource {

  private static final Logger logger = LoggerFactory.getLogger(NetworkResource.class);

  private NetworkDAO networkDAO;
  private final NetworkRepository networkRepository;

  public NetworkResource(DBI dbi) {
    this.networkDAO = dbi.onDemand(NetworkDAO.class);
    this.networkRepository = new DbiNetworkRepository(dbi);
  }

  @GET
  @Path("/ip}")
  @Produces(MediaType.APPLICATION_JSON)
//  @Consumes(MediaType.APPLICATION_JSON)
  public Response pppoeUserLastIp() {
//    logger.debug("PPPoE login '{}'", login);
//    Map<String, Object> lastIp = new LinkedHashMap<>(networkRepository.findPppoeUserLastIp(login));
//    logger.debug("PPPoE last IP '{}'", lastIp);
//    return Response.ok(ImmutableMap.of("lastIp", lastIp)).build();
    return Response.ok().build();
  }

  @PUT
  @Path("pppoe/{serviceId}")
  @Produces({"application/json; charset=UTF-8"})
  public Response updateServicePppoe(
      @PathParam("serviceId") long serviceId,
      Map<String, Object> updateBody) {
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
      Map<String, Object> updateBody) {
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
        logger.debug("updating DHCP of service '{}'", serviceId);
        final int networkId = dhcpUpdate.asMap().get("network_id").asIntegerOr(-1);
        final int port = dhcpUpdate.asMap().get("port").asIntegerOr(-1);
        checkState(networkId > 0, "new switch network_id not provided");
        checkState(port >= 0, "new switch port not provided");
        networkRepository.bindDhcp(serviceId, networkId, port);
        final Map<String, Object> propsUpdate = dhcpUpdate.asMap().map();
        propsUpdate.remove("network_id");
        propsUpdate.remove("port");
        if (!propsUpdate.isEmpty()) {
          networkRepository.updateDhcp(serviceId, propsUpdate);
        }
      }
    }
    return Response.ok(ImmutableMap.of()).build();
  }

  @GET
  @Path("{country}/devices")
  @Produces({"application/json; charset=UTF-8"})
  public Response findDevicesByCountry(
      @PathParam("country") String countryParam,
      @QueryParam("deviceType") String deviceTypeParam) {
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

}
