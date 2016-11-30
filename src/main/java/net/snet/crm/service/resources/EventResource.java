package net.snet.crm.service.resources;

import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.shared.event.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/events")
public class EventResource {
  private static final Logger log = LoggerFactory.getLogger(EventResource.class);

  private EventLog eventLog;

  public EventResource(EventLog eventLog) {
    this.eventLog = eventLog;
  }

  @GET
  @Path("/")
  @Produces({"application/json; charset=UTF-8"})
  public Response findEvents(@Context UriInfo uriInfo) {
    final MultivaluedMap<String, String> params = uriInfo.getQueryParameters(true);
    log.debug(params.toString());

    return Response.ok(ImmutableMap.of()).build();
  }
}
