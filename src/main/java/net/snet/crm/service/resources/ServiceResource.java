package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import net.snet.crm.domain.model.agreement.CrmRepository;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.todo.TodoRepository;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import net.snet.crm.infrastructure.addresses.AddressRepository;
import net.snet.crm.infrastructure.addresses.PlaceRepository;
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

@Path("/services")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceResource
{
  private static final Logger logger = LoggerFactory.getLogger(ServiceResource.class);
  private final CrmRepository crmRepository;
  private final NetworkRepository networkRepository;
  private final TodoRepository todoRepository;
  private final AddressRepository addresses;
  private final PlaceRepository places;

  public ServiceResource(
      CrmRepository crmRepository,
      NetworkRepository networkRepository,
      TodoRepository todoRepository,
      AddressRepository addresses,
      PlaceRepository places)
  {
    this.crmRepository = crmRepository;
    this.networkRepository = networkRepository;
    this.todoRepository = todoRepository;
    this.addresses = addresses;
    this.places = places;
  }

  @GET
  @Path("/conflicting-authentications")
  public Response conflictingAuthentications()
  {
    return Response.ok(ImmutableMap.of(
        "services", networkRepository.findConflictingAuthentications())).build();
  }

  @GET
  @Path("/{serviceId}/comments")
  public Response serviceComments(@PathParam("serviceId") long serviceId)
  {
    return Response.ok(ImmutableMap.of(
        "comments", todoRepository.findServiceComments(serviceId))).build();
  }

  @GET
  @Path("/{serviceId}/todos")
  public Response serviceTodos(@PathParam("serviceId") long serviceId)
  {
    final List<Data> comments = todoRepository.findServiceComments(serviceId);
    final Multimap<Long, Data> todoComments = LinkedListMultimap.create();
    for (Data comment : comments)
    {
      todoComments.put(comment.longOf("todo_id"), comment);
    }
    List<Data> todos = Lists.newArrayList();
    for (Long todoId : todoComments.keySet())
    {
      final Data todo = todoRepository.findTodo(todoId);
      todo.asModifiableContent().put("comments", todoComments.get(todoId));
      todos.add(todo);
    }
    return Response.ok(ImmutableMap.of("todos", todos)).build();
  }

  @PUT
  @Path("/{serviceId}")
  public Response updateService(
      @PathParam("serviceId") long serviceId,
      Data body)
  {
    checkState(body.hasData("services"), "no service update body sent");
    final Data data = body.dataOf("services");
    logger.debug("updating service: '{}'", data);
    resolveAddressUpdateInPlace(data);
    crmRepository.updateService(serviceId, data);
    return Response.ok(ImmutableMap.of()).build();
  }

  private void resolveAddressUpdateInPlace(Data data)
  {
    final Map<String, Object> update = data.asModifiableContent();
    final Long addressId = data.hasValue("address_id") ? data.longOf("address_id") : null;
    update.put("address_id", addressId);
    final String addressPlace = data.optStringOf("address_place");
    final String place = data.optStringOf("place");
    if (!place.isEmpty() && !place.equals(addressPlace)) {
      final long placeId = places.add(
          MapData.of(ImmutableMap.<String, Object>of("gps_cord", place)));
      update.put("place_id", placeId);
    }
    else {
      if (!data.hasValue("address_place_id"))
      {
        throw new RuntimeException("GPS coordinates are missing");
      }
      update.put("place_id", data.longOf("address_place_id"));
    }
    update.remove("address_place_id");
    update.remove("address_place");
    update.remove("place");
  }

  @GET
  @Path("/{serviceId}/dhcp")
  public Response serviceDhcp(@PathParam("serviceId") long serviceId)
  {
    return Response.ok(ImmutableMap.of("dhcp", networkRepository.findServiceDhcp(serviceId))).build();
  }

  @GET
  @Path("/{serviceId}/dhcp-wireless")
  public Response serviceDhcpWireless(@PathParam("serviceId") long serviceId)
  {
    return Response.ok(ImmutableMap.of("dhcp_wireless", networkRepository.findServiceDhcpWireless(serviceId))).build();
  }

  @GET
  @Path("/{serviceId}/pppoe")
  public Response servicePppoe(@PathParam("serviceId") long serviceId)
  {
    return Response.ok(ImmutableMap.of("pppoe", networkRepository.findServicePppoe(serviceId))).build();
  }

  @GET
  public Response servicesByQuery(
      @QueryParam("q") Optional<String> query,
      @QueryParam("country") Optional<String> country,
      @QueryParam("isActive") Optional<String> isActiveValue,
      @Context UriInfo uriInfo
  )
  {
    final Boolean isActive = resolveIsActive(isActiveValue);
    List<Map<String, Object>> services = crmRepository.findService(query.or(""), country.or(""), isActive);
    logger.debug(uriInfo.getRequestUri().getQuery());
    return Response.ok(ImmutableMap.of(
        "services", services,
        "query", uriInfo.getRequestUri().getQuery()
    )).build();
  }

  private Boolean resolveIsActive(Optional<String> isActiveValue)
  {
    if (!isActiveValue.isPresent() || "null".equals(isActiveValue.get().toLowerCase()))
    {
      return null;
    }
    return Boolean.valueOf(isActiveValue.get());
  }

  @GET
  @Path("/{serviceId}")
  @Timed(name = "get-requests")
  public Map<String, Object> serviceById(@PathParam("serviceId") long serviceId)
  {
    logger.debug("fetching service by id '{}'", serviceId);
    return crmRepository.findServiceById(serviceId);
  }

  @POST
  @Path("/{serviceId}/connections")
  @Timed(name = "post-request")
  public Response insertConnection(
      @PathParam("serviceId") long serviceId,
      Optional<Map<String, Object>> connectionData,
      @Context UriInfo uriInfo)
  {
    logger.debug("inserting new connection for service id '{}'", serviceId);
    Map<String, Object> service = crmRepository.findServiceById(serviceId);
    checkNotNull(service.get("id"), "service with id '%s' does not exist", serviceId);
    Map<String, Object> connection = crmRepository.insertConnection(serviceId);
    if (connectionData.isPresent())
    {
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
