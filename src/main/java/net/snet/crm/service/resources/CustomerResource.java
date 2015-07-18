package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import net.snet.crm.service.bo.CustomerSearch;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.dao.CustomerDAO;
import net.snet.crm.service.utils.Utils;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

	private static final Logger logger = LoggerFactory.getLogger(CustomerResource.class);

	private final String FROM_CHARS = "ÁĄÄČĆĎÉĚĘËÍŁŇŃÓÖŘŠŚŤÚŮÜÝŽŻŹáąäčćďéěęëíłňńóöřšśťúůüýžżź.-,;:&+? ";
	private final String TO_CHARS = "aaaccdeeeeilnnoorsstuuuyzzzaaaccdeeeeilnnoorsstuuuyzzz";

	private final Pattern LEDGER_IMPORT = Pattern.compile("ledger-(\\w\\w)-import");

	private @Context UriInfo uriInfo;

	private CustomerDAO customerDAO;
	private final DBI dbi;
	private final CrmRepository repository;

	public CustomerResource(DBI dbi, CrmRepository repository) {
		this.customerDAO = dbi.onDemand(CustomerDAO.class);
		this.dbi = dbi;
		this.repository = repository;
	}

	@GET
	@Produces({"application/json; charset=UTF-8"})
	@Path("/{customerId}")
	@Timed(name = "get-requests")
	public Map<String, Object> getCustomerById(@PathParam("customerId") long id) {

		final HashMap<String, Object> customersMap = new HashMap<String, Object>();

		customersMap.put("customer", customerDAO.findById(id));

		return customersMap;
	}

	@GET
	@Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Map<String, Object> getCustomersByQuery(
			@QueryParam("qn") Optional<String> queryName,
			@QueryParam("q") Optional<String> name,
			@QueryParam("country") Optional<String> country)
	{
		final HashMap<String, Object> customersMap = new HashMap<String, Object>();
		customersMap.put("customers", Lists.newArrayList());
		if (queryName.isPresent()) {
			String query = queryName.get().toLowerCase();
			logger.debug("querying customers for named query '{}'", query);
			Iterator<Map<String, Object>> customers = Iterators.emptyIterator();
			Matcher ledgerMatcher = LEDGER_IMPORT.matcher(query);
			if (ledgerMatcher.matches()) {
				String ledgerCountry = ledgerMatcher.group(1);
				final long countryId = ledgerCountry.equals("cs") ? 10 : (ledgerCountry.equals("pl") ? 20: 0);
				if (countryId > 0) {
					logger.debug("customers import query for country '{}:{}'", ledgerCountry, countryId);
					customers = dbi.withHandle(new HandleCallback<Iterator<Map<String, Object>>>() {
						@Override
						public Iterator<Map<String, Object>> withHandle(Handle handle) throws Exception {
							String sql = "SELECT * FROM customers\n" +
									"  WHERE country = :countryId\n" +
									"    AND is_active\n" +
									"    AND (\n" +
									"        synchronized IS NULL\n" +
									"        OR synchronized < updated\n" +
									"    )\n" +
									"    AND public_id != '9999999'";
							return handle.createQuery(sql)
									.bind("countryId", countryId)
									.list()
									.iterator();
						}
					});
				} else {
					logger.debug("customers import query for unknown country '{}:{}'", ledgerCountry, countryId);
				}
			}
			customersMap.put("customers", Lists.newArrayList(customers));
		} else {
			if (name.isPresent()) {
				Iterator<CustomerSearch> customers;
				if (country.isPresent()) {
					final String countryLabel = country.get().toLowerCase();
					final long countryId = "cz".equals(countryLabel) ? 10 : ("pl".equals(countryLabel) ? 20 : 0);
					customers = customerDAO.getCustomersByNameAndCountry(
							"%" + Utils.replaceChars(name.get(), FROM_CHARS, TO_CHARS) + "%",
							FROM_CHARS, TO_CHARS, countryId);
				} else {
					customers = customerDAO.getCustomersByName(
							"%" + Utils.replaceChars(name.get(), FROM_CHARS, TO_CHARS) + "%",FROM_CHARS, TO_CHARS);
				}
				List<Map<String, Object>> retCustomers = new ArrayList<Map<String, Object>>();
				while (customers.hasNext()) {
					CustomerSearch customer = customers.next();
					Map<String, Object> customerMap = Maps.newLinkedHashMap();
					customerMap.put("id", customer.getId());
					customerMap.put("name", customer.getName());
					customerMap.put("street", customer.getStreet());
					customerMap.put("city", customer.getCity());
					customerMap.put("postal_code", customer.getPostalCode());
					List<Long> agreementIds = Lists.newArrayList();
					List<Map<String, Object>> agreements = repository.findAgreementsByCustomerId(customer.getId());
					for (Map<String, Object> agreement : agreements) {
						agreementIds.add(Long.valueOf(agreement.get("id").toString()));
					}
					customerMap.put("agreements", agreementIds);
					retCustomers.add(customerMap);
				}
				customersMap.put("customers", retCustomers);
			}
		}
		return customersMap;
	}

	@PUT
	@Timed(name = "put-requests")
	public Response updateCustomers(Map<String, Object> updates) {
		logger.debug("update customers called");
		HashMap<String, Object> response = Maps.newHashMap();
		List<Map<String, Object>> updated = Lists.newArrayList();
		try {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> customerUpdates = (List<Map<String, Object>>) updates.get("customers");
			for (Map<String, Object> customerUpdate : customerUpdates) {
				logger.debug("updating customer with '{}'", customerUpdate);
				try {
					final long id = Long.valueOf(customerUpdate.get("id").toString());
					if (customerUpdate.containsKey("synchronized")) {
						if (customerUpdate.get("synchronized") != null) {
							final DateTime synchronizedOn = DateTime.parse(customerUpdate.get("synchronized").toString());
							dbi.withHandle(new HandleCallback<Void>() {
								@Override
								public Void withHandle(Handle handle) throws Exception {
									int changed = handle.createStatement(
											"UPDATE customers\n" +
													"  SET synchronized = :synchronizedOn\n" +
													"  WHERE id = :id\n")
											.bind("id", id)
											.bind("synchronizedOn", synchronizedOn.toDate())
											.execute();
									if (changed != 1) {
										throw new RuntimeException("customer with id '" + id + "' not found, cannot update");
									}
									logger.debug("customer with id '{}' was synchronized on '{}'", id, synchronizedOn);
									return null;
								}
							});
						} else {
							dbi.withHandle(new HandleCallback<Object>() {
								@Override
								public Void withHandle(Handle handle) throws Exception {
									int changed = handle.createStatement(
											"UPDATE customers\n" +
													"  SET synchronized = updated\n" +
													"  WHERE id = :id\n")
											.bind("id", id)
											.execute();
									if (changed != 1) {
										throw new RuntimeException("customer with id '" + id + "' not found, cannot update");
									}
									logger.debug("customer with id '{}' was NOT synchronized", id);
									return null;
								}
							});
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage());
					customerUpdate.put("errors", ImmutableList.of(ImmutableMap.of("message", "" + e.getMessage())));
				}
				updated.add(customerUpdate);
			}
		} catch (Exception e) {
			response.put("errors", ImmutableList.of(ImmutableMap.of("message", "" + e.getMessage())));
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
						.entity(response)
						.build());
		}
		response.put("customers", updated);
		return Response.ok(response).build();
	}

	@POST
	@Timed(name = "post-requests")
	public Response createCustomer(Map<String, Object> customerData) {
		logger.debug("creating new customer");
		@SuppressWarnings("unchecked")
		Map<String, Object> customer = (Map<String, Object>) customerData.get("customers");
		final Map<String, Object> customersMap = repository.insertCustomer(customer);
		return Response.created(uriInfo.getAbsolutePathBuilder().path("/" + customersMap.get("id")).build())
				.entity(ImmutableMap.of("customers", customersMap))
				.build();
	}

	@POST
	@Path("/{customerId}/agreements")
	@Timed(name = "post-requests")
	public Response createCustomerAgreement(@PathParam("customerId") long customerId,
	                                        Map<String, Object> agreementData) {
		@SuppressWarnings("unchecked")
		Map<String, Object> agreementPrototype = (Map<String, Object>) agreementData.get("agreements");
		String country = agreementPrototype.get("country").toString();
		logger.debug("creating new agreement for customer id '{}' in country '{}'", customerId, country);
		Map<String, Object> customer = repository.findCustomerById(customerId);
		final Map<String, Object> agreement = repository.insertAgreement((Long) customer.get("id"), country);
		return Response.created(uriInfo.getAbsolutePathBuilder()
				.replacePath("/agreements/" + agreement.get("id")).build())
				.entity(ImmutableMap.of("agreements", agreement))
				.build();
	}
}

