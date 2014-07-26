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

@Path("/agreements")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AgreementResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(AgreementResource.class);

	private @Context
	UriInfo uriInfo;
	private final CrmRepository repository;

	public AgreementResource(CrmRepository repository) {
		this.repository = repository;
	}

	@GET
	@Path("/{agreementId}")
	@Timed(name = "get-requests")
	public Map<String, Object> agreementById(@PathParam("agreementId") long agreementId) {
		LOGGER.debug("fetching agreement by id '{}'", agreementId);
		return repository.findAgreementById(agreementId);
	}

	@POST
	@Path("/{agreementId}/services")
	@Timed(name = "post-request")
	public Response insertService(@PathParam("agreementId") long agreementId) {
		LOGGER.debug("inserting new service for agreement id '{}'", agreementId);
		Map<String, Object> agreement = repository.findAgreementById(agreementId);
		checkNotNull(agreement.get("id"), "agreement with id '%s' does not exist", agreementId);
		Map<String, Object> service = repository.insertService(agreementId);
		return Response.created(uriInfo.getAbsolutePathBuilder()
				.replacePath("/services/" + service.get("id")).build())
				.entity(ImmutableMap.of("services", service))
				.build();
	}

}
