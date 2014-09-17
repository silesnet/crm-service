package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.sun.jersey.api.Responses;
import net.snet.crm.service.bo.Draft;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.dao.DraftDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("/drafts")
public class DraftResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(DraftResource.class);

	private final DraftDAO draftDAO;
	private final ObjectMapper objectMapper;
	private final CrmRepository crmRepository;

	public DraftResource(DraftDAO draftDAO, ObjectMapper objectMapper, CrmRepository crmRepository) {
		this.draftDAO = draftDAO;
		this.objectMapper = objectMapper;
		this.crmRepository = crmRepository;
	}

	@GET
	@Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Map<String, Object> getDraftsByUserId(@QueryParam("user_id") String userId) {
		LOGGER.debug("drafts called");

		final HashMap<String, Object> draftsMap = new HashMap<String, Object>();

		Iterator<Draft> drafts = draftDAO.findDraftsByUserId(userId);

		draftsMap.put("drafts", drafts);

		return draftsMap;
	}

	@GET
	@Path("/{draftId}")
	@Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Draft getDraftById(@PathParam("draftId") long draftId) {
		LOGGER.debug("drafts called");
		return draftDAO.findDraftById(draftId);
	}

	@POST
	@Timed(name = "post-requests")
	public String insertDraft(@QueryParam("user_id") String userId, String body) {
		LOGGER.debug("drafts called");
		return draftDAO.insertDraft(new Draft("service", userId, body)).toString();
	}

	@PUT
	@Path("/{draftId}")
	@Timed(name = "put-requests")
	public Response updateDraft(@PathParam("draftId") Integer draftId, String body) {
		LOGGER.debug("drafts called");

		draftDAO.updateDraft(body, draftId);

		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}")
	@Timed(name = "delete-requests")
	public Response deleteDraft(@PathParam("id") long id) throws IOException {
		LOGGER.debug("drafts called");
		Draft draft = draftDAO.findDraftById(id);
		if (draft != null) {
			if ("service".equals(draft.getType())) {
				Map<String, Object> draftData = objectMapper.readValue(draft.getData(),
						objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
				final long serviceId = getNestedLong("customer.service_id", draftData);
				final Map<String, Object> service = crmRepository.findServiceById(serviceId);
				if (service != null && "DRAFT".equals(service.get("status"))) {
					crmRepository.deleteService(serviceId);
					final Map<String, Object> connection = crmRepository.findConnectionByServiceId(serviceId);
					if (connection != null) {
						crmRepository.deleteConnection(serviceId);
					}
				}
				final long customerId = getNestedLong("customer.id", draftData);
				final Map<String, Object> customer = crmRepository.findCustomerById(customerId);
				if (customer != null && "DRAFT".equals(customer.get("customer_status"))) {
					crmRepository.deleteCustomer(customerId);
				}
				final long agreementId = getNestedLong("customer.agreement_id", draftData);
				final Map<String, Object> agreement = crmRepository.findAgreementById(agreementId);
				if (agreement != null && "DRAFT".equals(agreement.get("status"))) {
					crmRepository.updateAgreementStatus(agreementId, "AVAILABLE");
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
}

