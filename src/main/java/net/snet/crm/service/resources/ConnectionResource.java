package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import net.snet.crm.service.dao.CrmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/connections")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConnectionResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionResource.class);

	private @Context
	UriInfo uriInfo;
	private final CrmRepository repository;

	public ConnectionResource(CrmRepository repository) {
		this.repository = repository;
	}

	@GET
	@Path("/{serviceId}")
	@Timed(name = "get-requests")
	public Map<String, Object> connectionById(@PathParam("serviceId") long serviceId) {
		LOGGER.debug("fetching connection by service id '{}'", serviceId);
		return repository.findConnectionByServiceId(serviceId);
	}

	@PUT
	@Path("/{serviceId}")
	@Timed(name = "put-request")
	public Response updateConnection(@PathParam("serviceId") long serviceId, Map<String, Object> connectionData) {
		Map<String, Object> existingConnection = repository.findConnectionByServiceId(serviceId);
		checkNotNull(existingConnection, "connection for service '%s' does not exist", serviceId);
		Map<String, Object> connectionPrototype = (Map<String, Object>) connectionData.get("connections");
		Map<String, Object> connection = repository.updateConnection(serviceId, connectionPrototype.entrySet());
		return Response.ok(ImmutableMap.of("connections", connection)).build();
	}
}
