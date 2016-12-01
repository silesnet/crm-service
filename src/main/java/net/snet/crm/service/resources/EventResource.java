package net.snet.crm.service.resources;

import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.shared.event.Event;
import net.snet.crm.domain.shared.event.EventConstrain;
import net.snet.crm.domain.shared.event.EventLog;
import net.snet.crm.domain.shared.event.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.List;

@Path("/events")
@Produces({"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {
  private static final Logger log = LoggerFactory.getLogger(EventResource.class);

  private EventLog eventLog;

  public EventResource(EventLog eventLog) {
    this.eventLog = eventLog;
  }

  @GET
  public Response findEvents(@Context UriInfo uriInfo) {
    final MultivaluedMap<String, String> params = uriInfo.getQueryParameters(true);
    final List<Event> events = eventLog.events(constrain(params));
    log.debug(events.toString());
    return Response.ok(ImmutableMap.of("data", events)).build();
  }

  private EventConstrain constrain(MultivaluedMap<String, String> params) {
    final EventConstrain.Builder constrain = EventConstrain.builder();
    if (params.containsKey("pastEventId")) {
      constrain.eventsPastEventId(Long.valueOf(params.getFirst("pastEventId")));
    }
    if (params.containsKey("event")) {
      constrain.forEvent(Events.of(params.getFirst("event")));
    }
    if (params.containsKey("entity")) {
      if (params.containsKey("entityId")) {
        constrain.forEntityInstance(params.getFirst("entity"), Long.valueOf(params.getFirst("entityId")));
      } else {
        constrain.forEntity(params.getFirst("entity"));
      }
    }
    return constrain.build();
  }
}
