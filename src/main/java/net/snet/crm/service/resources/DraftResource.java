package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import com.sun.jersey.api.Responses;
import net.snet.crm.service.bo.Draft;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.dao.CrmRepositoryJdbi;
import net.snet.crm.service.dao.DraftDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;

@Path("/drafts")
public class DraftResource {
	private static final Map<String, Long> COUNTRIES = ImmutableMap.of("CZ", 10L, "PL", 20L);
	private static final Logger logger = LoggerFactory.getLogger(DraftResource.class);

	private @Context
	UriInfo uriInfo;

	private final DraftDAO draftDAO;
	private final ObjectMapper objectMapper;
	private final CrmRepository crmRepository;
	private final MapType entityMapType;

	public DraftResource(DraftDAO draftDAO, ObjectMapper objectMapper, CrmRepository crmRepository) {
		this.draftDAO = draftDAO;
		this.objectMapper = objectMapper;
		this.crmRepository = crmRepository;
		this.entityMapType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
	}

	@GET
	@Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Map<String, Object> getDraftsByUserId(@QueryParam("user_id") String userId) {
		logger.debug("drafts called");
		final Set<Draft> drafts = Sets.newLinkedHashSet();
		Iterator<Draft> userDrafts = draftDAO.findDraftsByUserId(userId, "DRAFT");
		Iterators.addAll(drafts, userDrafts);
		Map<String, Object> user = crmRepository.findUserByLogin(userId);
		checkState(user != null, "unknown user '%s'", userId);
		String rawRoles = user.get("roles").toString();
		Iterable<String> roles = Splitter.on(',').trimResults().split(rawRoles);
		for (String role : roles) {
			if ("ROLE_TECH_ADMIN".equals(role)) {
				List<Draft> submitted = Lists.newArrayList(draftDAO.findDraftsByStatus("SUBMITTED"));
				List<Map<String, Object>> subordinatesList = crmRepository.findUserSubordinates(userId);
				Set<String> subordinates = Sets.newHashSet();
				for (Map<String, Object> subordinate : subordinatesList) {
					subordinates.add(subordinate.get("login").toString());
				}
				for (Draft draft : submitted) {
					if (subordinates.contains(draft.getUserId())) {
						drafts.add(draft);
					}
				}
			}
			if ("ROLE_ACCOUNTING".equals(role)) {
				Iterator<Draft> reviewed = draftDAO.findDraftsByStatus("APPROVED");
				Iterators.addAll(drafts, reviewed);
			}
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

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Timed(name = "post-requests")
	@SuppressWarnings("unchecked")
	public Response insertDraft(Map<String, Object> draftRequestData) throws JsonProcessingException {
		Map<String, Object> draftRequest = (Map<String, Object>) draftRequestData.get("drafts");
		final String draftType = draftRequest.get("type").toString();
		final String userId = draftRequest.get("userId").toString();
		Map<String, Object> draftData = Maps.newLinkedHashMap();
		if ("service".equals(draftType)) {
			Map<String, Object> customer;
			final Optional<Long> customerId = getSafeLong("data.customer.id", draftRequest);
			if (customerId.isPresent()) {
				customer = crmRepository.findCustomerById(customerId.get());
			} else {
				final Optional<String> customerName = getSafe("data.customer.name", draftRequest);
				final Optional<String> customerCountry = getSafe("data.customer.country", draftRequest);
				checkState(customerName.isPresent() && customerCountry.isPresent(),
						"trying to create a new customer for a service draft, but customer name and/or country are not provided");
				customer = crmRepository.insertCustomer(ImmutableMap.of(
						"name", customerName.get(),
						"country", (Object) COUNTRIES.get(customerCountry.get())
				));
			}
			Map<String, Object> agreement;
			final Optional<Long> agreementId = getSafeLong("data.agreement.id", draftRequest);
			if (agreementId.isPresent()) {
				agreement = crmRepository.findAgreementById(agreementId.get());
				checkState(getNestedLong("id", customer) == getNestedLong("customer_id", agreement),
						"trying to create service draft where customer '%s' and agreement customer '%s' does not match",
						getNestedLong("id", customer), getNestedLong("customer_id", agreement));
			} else {
				final Optional<String> agreementCountry = getSafe("data.agreement.country", draftRequest);
				checkState(agreementCountry.isPresent(), "trying to create new customer agreement for a service draft {customer.id: %s}, but country is not specified",
						getNestedLong("id", customer));
				agreement = crmRepository.insertAgreement(getNestedLong("id", customer), agreementCountry.get());
			}
			Map<String, Object> service = crmRepository.insertService(getNestedLong("id", agreement));
			draftData.put("customer", customer);
			draftData.put("agreement", agreement);
			draftData.put("service", service);
		}
		final String draftDataString = objectMapper.writeValueAsString(draftData);
		final Integer draftId = draftDAO.insertDraft(new Draft(0L, draftType, userId, draftDataString, "DRAFT"));
		final Draft draft = draftDAO.findDraftById(draftId);
		return Response.created(uriInfo.getAbsolutePathBuilder()
				.replacePath("/drafts/{draftId}").build(draft.getId()))
				.entity(ImmutableMap.of("drafts", draft))
				.build();
	}

	@PUT
	@Path("/{draftId}")
	@Timed(name = "put-requests")
	public Response updateDraft(@PathParam("draftId") Integer draftId, String body) {
		logger.debug("drafts called");
		draftDAO.updateDraftData(body, draftId);
		return Response.ok().build();
	}

	@PUT
	@Path("new/{draftId}")
	@Timed(name = "put-requests")
	public Response updateDraftAndStatus(@PathParam("draftId") Long draftId, Map<String, Object> drafts) throws IOException {
		logger.debug("updating draft '{}'", draftId);
		final Map<String, Object> draft = (Map<String, Object>) drafts.get("drafts");
		Draft currentDraft = draftDAO.findDraftById(draftId);
		checkState(currentDraft != null, "trying to update draft '%s' that does not exist", draftId);
		final String currentDraftStatus = currentDraft.getStatus();
		final Optional<String> data = Optional.fromNullable((String) draft.get("data"));
		if (data.isPresent()) {
			draftDAO.updateDraftData(data.get(), draftId);
		}
		final Optional<String> status = Optional.fromNullable((String) draft.get("status"));
		if (status.isPresent()) {
			if ("ACCEPTED".equals(currentDraftStatus) && "IMPORTED".equals(status.get())) {
				logger.debug("importing draft '{}' into tables");
				final Map<String, Object> dataMap = (Map<String, Object>) objectMapper.readValue(currentDraft.getData(), Map.class);
				// import customer
				final Long customerId = getNestedLong("customer.id", dataMap);
				final Map<String, Object> customer = crmRepository.findCustomerById(customerId);
				checkState(customer != null, "customer '%s' does not exist", customerId);
				final Map<String, Object> customerFormMap = (Map<String, Object>) dataMap.get("customer");
				final CustomerForm customerForm = new CustomerForm(customerFormMap);
				final Map<String, Object> customerUpdate = customerForm.customerUpdate();
				customerUpdate.put("status", "ACTIVE");
				final Map<String, Object> updatedCustomer = crmRepository.updateCustomer(customerId, customerUpdate);
				// import agreement
				final long agreementId = getNestedLong("customer.agreement_id", dataMap);
				final Map<String, Object> agreement = crmRepository.findAgreementById(agreementId);
				checkState(agreement != null, "agreement '%s' does not exist", agreementId);
				if (!"ACTIVE".equals(agreement.get("status"))) {
					crmRepository.updateAgreementStatus(agreementId, "ACTIVE");
				}
				long contractNumber = agreementId % CrmRepositoryJdbi.SERVICE_COUNTRY_MULTIPLIER;
				String agreements = "" + contractNumber;
				Optional<String> currentAgreements = Optional.fromNullable((String) updatedCustomer.get("contract_no"));
				if (currentAgreements.isPresent() && currentAgreements.get().trim().length() > 0) {
					agreements = currentAgreements.get().toString().trim() + ", " + contractNumber;
				}
				crmRepository.setCustomerAgreements(customerId, agreements);
				// import service
				final long serviceId = getNestedLong("customer.service_id", dataMap);
				final Map<String, Object> service = crmRepository.findServiceById(serviceId);
				checkState(service != null, "service '%s' does not exist", customerId);
				final Map<String, Object> serviceFormMap = (Map<String, Object>) dataMap.get("service");
				final ServiceForm serviceForm = new ServiceForm(serviceFormMap);
				final Map<String, Object> serviceUpdate = serviceForm.serviceUpdate();
				serviceUpdate.put("status", "ACTIVE");
				crmRepository.updateService(serviceId, serviceUpdate);
			}
			draftDAO.updateDraftStatus(status.get(), draftId);
		}
		return Response.noContent().build();
	}

	@DELETE
	@Path("/{id}")
	@Timed(name = "delete-requests")
	public Response deleteDraft(@PathParam("id") long id) throws IOException {
		Draft draft = draftDAO.findDraftById(id);
		if (draft != null) {
			if ("service".equals(draft.getType())) {
				Map<String, Object> draftData = objectMapper.readValue(draft.getData(), entityMapType);
				final Optional<Long> serviceId = getSafeLong("service.service_id", draftData);
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
				final Optional<Long> agreementId = getSafeLong("service.agreement_id", draftData);
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

	private Optional<String> getSafe(String path, Map<String, Object> map) {
		try {
			return Optional.of(getNested(path, map).toString());
		} catch (Exception e) {
			return Optional.absent();
		}
	}


}

