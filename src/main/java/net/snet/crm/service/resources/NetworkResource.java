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
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
  @Path("{country}/devices")
  @Produces({"application/json; charset=UTF-8"})
  public Response findDevicesByCountry(
      @PathParam("country") String countryParam,
      @QueryParam("deviceType") String deviceTypeParam) {
    final Country country = Country.valueOf(countryParam.toUpperCase());
    final DeviceType deviceType = DeviceType.valueOf(deviceTypeParam.toUpperCase());
    final List<Map<String, Object>> devices =
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
  @Path("/routers")
  @Produces({"application/json; charset=UTF-8"})
  @Timed(name = "get-requests")
  public Map<String, Object> getAllNetworks() {

    final HashMap<String, Object> coreRoutersMap = new HashMap<String, Object>();

    Iterator<Network> routers = networkDAO.allMasters();

    coreRoutersMap.put("core_routers", routers);

    return coreRoutersMap;
  }

  @GET
  @Path("/ssids")
  @Produces({"application/json; charset=UTF-8"})
  @Timed(name = "get-requests")
  public Map<String, Object> getAllSsids() {

    final HashMap<String, Object> networksMap = new HashMap<String, Object>();

    Iterator<Network> networks = networkDAO.allSsids();

    networksMap.put("ssids", networks);

    return networksMap;
  }

}
