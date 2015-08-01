package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.snet.crm.service.dao.CrmRepository;
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

@Path("/services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceResource.class);

  private
  @Context
  UriInfo uriInfo;
  private final CrmRepository repository;

  public ServiceResource(CrmRepository repository) {
    this.repository = repository;
  }

  @GET
  @Path("/{serviceId}/dhcp")
  public Response serviceDhcp(@PathParam("serviceId") long serviceId) {
    return Response.ok(ImmutableMap.of("dhcp", repository.serviceDhcp(serviceId))).build();
  }

  @GET
  @Path("/{serviceId}/pppoe")
  public Response servicePppoe(@PathParam("serviceId") long serviceId) {
    return Response.ok(ImmutableMap.of("pppoe", repository.servicePppoe(serviceId))).build();
  }

  @GET
  public Response servicesByQuery(
      @QueryParam("q") Optional<String> query,
      @QueryParam("country") Optional<String> country) {
    List<Map<String, Object>> services = repository.findService(query.or(""), country.or(""));
    return Response.ok(ImmutableMap.of("services", services)).build();
  }

  @GET
  @Path("/{serviceId}")
  @Timed(name = "get-requests")
  public Map<String, Object> serviceById(@PathParam("serviceId") long serviceId) {
    LOGGER.debug("fetching service by id '{}'", serviceId);
    return repository.findServiceById(serviceId);
  }

  @POST
  @Path("/{serviceId}/connections")
  @Timed(name = "post-request")
  public Response insertConnection(@PathParam("serviceId") long serviceId, Optional<Map<String, Object>> connectionData) {
    LOGGER.debug("inserting new connection for service id '{}'", serviceId);
    Map<String, Object> service = repository.findServiceById(serviceId);
    checkNotNull(service.get("id"), "service with id '%s' does not exist", serviceId);
    Map<String, Object> connection = repository.insertConnection(serviceId);
    if (connectionData.isPresent()) {
      @SuppressWarnings("unchecked")
      Map<String, Object> connectionPrototype = (Map<String, Object>) connectionData.get().get("connections");
      connection = repository.updateConnection(serviceId, connectionPrototype.entrySet());
    }
    return Response.created(uriInfo.getAbsolutePathBuilder()
        .replacePath("/connections/" + connection.get("service_id")).build())
        .entity(ImmutableMap.of("connections", connection))
        .build();
  }

}
