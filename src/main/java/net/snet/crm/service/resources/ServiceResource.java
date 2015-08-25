package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.utils.Entities.Value;
import net.snet.crm.service.utils.Entities.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.service.utils.Entities.valueMapOf;

@Path("/services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {
  private static final Logger logger = LoggerFactory.getLogger(ServiceResource.class);

  private
  @Context
  UriInfo uriInfo;
  private final CrmRepository crmRepository;
  private final NetworkRepository networkRepository;

  public ServiceResource(CrmRepository crmRepository, NetworkRepository networkRepository) {
    this.crmRepository = crmRepository;
    this.networkRepository = networkRepository;
  }

  @PUT
  @Path("/{serviceId}")
  public Response updateService(
      @PathParam("serviceId") long serviceId,
      Map<String, Object> updateBody) {
    ValueMap update = valueMapOf(updateBody);
    Value serviceUpdate = update.get("services");
    checkState(!serviceUpdate.isNull(), "no service update body sent");
    logger.debug("updating service '{}'", serviceId);
    Value dhcpUpdate = serviceUpdate.asValueMap().get("dhcp");
    if (!dhcpUpdate.isNull()) {
      final int networkId = dhcpUpdate.asValueMap().get("network_id").asIntegerOr(-1);
      final int port = dhcpUpdate.asValueMap().get("port").asIntegerOr(-1);
      checkState(networkId > 0, "new switch network_id not provided");
      checkState(port >= 0, "new switch port not provided");
      networkRepository.bindDhcp(serviceId, networkId, port);
    }
    return Response.ok(ImmutableMap.of()).build();
  }

  @GET
  @Path("/{serviceId}/dhcp")
  public Response serviceDhcp(@PathParam("serviceId") long serviceId) {
    return Response.ok(ImmutableMap.of("dhcp", crmRepository.serviceDhcp(serviceId))).build();
  }

  @DELETE
  @Path("/{serviceId}/dhcp")
  public Response deleteDhcp(@PathParam("serviceId") long serviceId) {
    crmRepository.deleteDhcp(serviceId);
    return Response.noContent().build();
  }

  @GET
  @Path("/{serviceId}/pppoe")
  public Response servicePppoe(@PathParam("serviceId") long serviceId) {
    return Response.ok(ImmutableMap.of("pppoe", crmRepository.servicePppoe(serviceId))).build();
  }

  @GET
  public Response servicesByQuery(
      @QueryParam("q") Optional<String> query,
      @QueryParam("country") Optional<String> country) {
    List<Map<String, Object>> services = crmRepository.findService(query.or(""), country.or(""));
    return Response.ok(ImmutableMap.of("services", services)).build();
  }

  @GET
  @Path("/{serviceId}")
  @Timed(name = "get-requests")
  public Map<String, Object> serviceById(@PathParam("serviceId") long serviceId) {
    logger.debug("fetching service by id '{}'", serviceId);
    return crmRepository.findServiceById(serviceId);
  }

  @POST
  @Path("/{serviceId}/connections")
  @Timed(name = "post-request")
  public Response insertConnection(@PathParam("serviceId") long serviceId, Optional<Map<String, Object>> connectionData) {
    logger.debug("inserting new connection for service id '{}'", serviceId);
    Map<String, Object> service = crmRepository.findServiceById(serviceId);
    checkNotNull(service.get("id"), "service with id '%s' does not exist", serviceId);
    Map<String, Object> connection = crmRepository.insertConnection(serviceId);
    if (connectionData.isPresent()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> connectionPrototype = (Map<String, Object>) connectionData.get().get("connections");
      connection = crmRepository.updateConnection(serviceId, connectionPrototype.entrySet());
    }
    return Response.created(uriInfo.getAbsolutePathBuilder()
        .replacePath("/connections/" + connection.get("service_id")).build())
        .entity(ImmutableMap.of("connections", connection))
        .build();
  }

}
