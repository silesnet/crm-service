package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.sun.jersey.api.Responses;
import net.snet.crm.service.bo.Draft;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.dao.DraftDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Path("/drafts")
public class DraftResource {

	private static final Logger logger = LoggerFactory.getLogger(DraftResource.class);

	private final DraftDAO draftDAO;
	private final ObjectMapper objectMapper;
	private final CrmRepository crmRepository;
	private MapType entityMapType;

	public DraftResource(DraftDAO draftDAO, ObjectMapper objectMapper, CrmRepository crmRepository) {
		this.draftDAO = draftDAO;
		this.objectMapper = objectMapper;
		this.crmRepository = crmRepository;
		entityMapType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
	}

	@GET
	@Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Map<String, Object> getDraftsByUserId(@QueryParam("user_id") Optional<String> userId, @QueryParam("status") Optional<String> status) {
		logger.debug("drafts called");
		final Set<Draft> drafts = Sets.newLinkedHashSet();
		if (userId.isPresent()) {
			Iterator<Draft> userDrafts = draftDAO.findDraftsByUserId(userId.get(), "DRAFT");
			Iterators.addAll(drafts, userDrafts);
		}
		if (status.isPresent()) {
			final Iterator<Draft> draftsWithStatus = draftDAO.findDraftsByStatus(status.get());
			Iterators.addAll(drafts, draftsWithStatus);
		}
		return ImmutableMap.of("drafts", (Object) drafts);
	}

	@GET
	@Path("/{draftId}")
	@Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Draft getDraftById(@PathParam("draftId") long draftId) {
		logger.debug("drafts called");
		return draftDAO.findDraftById(draftId);
	}

	@POST
	@Timed(name = "post-requests")
	public String insertDraft(@QueryParam("user_id") String userId, String body) {
		logger.debug("drafts called");
		return draftDAO.insertDraft(new Draft(0L, "service", userId, body, "DRAFT")).toString();
	}

	@PUT
	@Path("/{draftId}")
	@Timed(name = "put-requests")
	public Response updateDraft(@PathParam("draftId") Integer draftId, String body) {
		logger.debug("drafts called");

		draftDAO.updateDraft(body, draftId);

		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}")
	@Timed(name = "delete-requests")
	public Response deleteDraft(@PathParam("id") long id) throws IOException {
		Draft draft = draftDAO.findDraftById(id);
		if (draft != null) {
			if ("service".equals(draft.getType())) {
				Map<String, Object> draftData = objectMapper.readValue(draft.getData(), entityMapType);
				final Optional<Long> serviceId = getSafeLong("customer.service_id", draftData);
				if (serviceId.isPresent()) {
					final Map<String, Object> service = crmRepository.findServiceById(serviceId.get());
					if (service != null && "DRAFT".equals(service.get("status"))) {
						crmRepository.deleteService(serviceId.get());
						final Map<String, Object> connection = crmRepository.findConnectionByServiceId(serviceId.get());
						if (connection != null) {
							crmRepository.deleteConnection(serviceId.get());
						}
					}
				}
				final Optional<Long> customerId = getSafeLong("customer.id", draftData);
				if (customerId.isPresent()) {
					final Map<String, Object> customer = crmRepository.findCustomerById(customerId.get());
					if (customer != null && "DRAFT".equals(customer.get("customer_status"))) {
						crmRepository.deleteCustomer(customerId.get());
					}
				}
				final Optional<Long> agreementId = getSafeLong("customer.agreement_id", draftData);
				if (agreementId.isPresent()) {
					final Map<String, Object> agreement = crmRepository.findAgreementById(agreementId.get());
					if (agreement != null && "DRAFT".equals(agreement.get("status"))) {
						crmRepository.updateAgreementStatus(agreementId.get(), "AVAILABLE");
					}
				}
			}
			draftDAO.deleteDraftById(id);
			return Response.noContent().build();
		}
		return Responses.notFound().build();
	}

	@SuppressWarnings("unchecked")
	private Object getNested(String path, Map<String, Object> map) {
		Object value = map;
		for (String key : Splitter.on('.').split(path)) {
			value = ((Map<String, Object>) value).get(key);
		}
		return value;
	}

	private long getNestedLong(String path, Map<String, Object> map) {
		return Long.valueOf(getNested(path, map).toString());
	}

	private Optional<Long> getSafeLong(String path, Map<String, Object> map) {
		try {
			return Optional.of(getNestedLong(path, map));
		} catch (Exception e) {
			return Optional.absent();
		}
	}


}

