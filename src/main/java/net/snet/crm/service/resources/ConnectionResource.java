package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import net.snet.crm.service.dao.CrmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
}
