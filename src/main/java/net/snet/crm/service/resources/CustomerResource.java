package net.snet.crm.service.resources;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.yammer.metrics.annotation.Timed;
import net.snet.crm.service.bo.CustomerSearch;
import net.snet.crm.service.dao.CustomerDAO;
import net.snet.crm.service.utils.Utils;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerResource.class);

	private final String FROM_CHARS = "ÁĄÄČĆĎÉĚĘËÍŁŇŃÓÖŘŠŚŤÚŮÜÝŽŻŹáąäčćďéěęëíłňńóöřšśťúůüýžżź.-,;:&+? ";
	private final String TO_CHARS = "aaaccdeeeeilnnoorsstuuuyzzzaaaccdeeeeilnnoorsstuuuyzzz";

	private final Pattern LEDGER_IMPORT = Pattern.compile("ledger-(\\w\\w)-import");

	private CustomerDAO customerDAO;
	private final DBI dbi;

	public CustomerResource(DBI dbi) {
		this.customerDAO = dbi.onDemand(CustomerDAO.class);
		this.dbi = dbi;
	}

	@GET
	@Produces({"application/json; charset=UTF-8"})
	@Path("/{customerId}")
	@Timed(name = "get-requests")
	public Map<String, Object> getCustomerById(@PathParam("customerId") long id) {
		LOGGER.debug("customers called");

		final HashMap<String, Object> customersMap = new HashMap<String, Object>();

		customersMap.put("customer", customerDAO.findById(id));

		return customersMap;
	}

	@GET
	@Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Map<String, Object> getCustomersByQuery(@QueryParam("qn") Optional<String> queryName, @QueryParam("q") Optional<String> name) {
		LOGGER.debug("customers called");

		final HashMap<String, Object> customersMap = new HashMap<String, Object>();
		customersMap.put("customers", Lists.newArrayList());
		if (queryName.isPresent()) {
			String query = queryName.get().toLowerCase();
			LOGGER.debug("querying customers for named query '{}'", query);
			Iterator<Map<String, Object>> customers = Iterators.emptyIterator();
			Matcher ledgerMatcher = LEDGER_IMPORT.matcher(query);
			if (ledgerMatcher.matches()) {
				String country = ledgerMatcher.group(1);
				final long countryId = country.equals("cs") ? 10 : (country.equals("pl") ? 20: 0);
				if (countryId > 0) {
					LOGGER.debug("customers import query for country '{}:{}'", country, countryId);
					customers = dbi.withHandle(new HandleCallback<Iterator<Map<String, Object>>>() {
						@Override
						public Iterator<Map<String, Object>> withHandle(Handle handle) throws Exception {
							String sql = "SELECT * FROM customers\n" +
									"  WHERE country = :countryId\n" +
									"    AND is_active\n" +
									"    AND (\n" +
									"        synchronized IS NULL\n" +
									"        OR synchronized <= updated\n" +
									"    )\n" +
									"    AND public_id != '9999999'";
							return handle.createQuery(sql)
									.bind("countryId", countryId)
									.list()
									.iterator();
						}
					});
				} else {
					LOGGER.debug("customers import query for unknown country '{}:{}'", country, countryId );
				}
			}
			customersMap.put("customers", Lists.newArrayList(customers));
		} else {
			if (name.isPresent()) {
				Iterator<CustomerSearch> customers = customerDAO.getCustomersByName("%" + Utils.replaceChars(name.get(), FROM_CHARS, TO_CHARS) + "%", FROM_CHARS, TO_CHARS);

				List<CustomerSearch> retCustomers = new ArrayList<CustomerSearch>();

				while (customers.hasNext()) {
					retCustomers.add(customers.next());
				}
				customersMap.put("customers", retCustomers);
			}
		}

		return customersMap;
	}

	@PUT
	@Timed(name = "get-requests")
	public Response updateCustomers(Map<String, Object> updates) {
		LOGGER.debug("update customers called");
		HashMap<String, Object> response = Maps.newHashMap();
		List<Map> updated = Lists.newArrayList();
		try {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> customerUpdates = (List<Map<String, Object>>) updates.get("customers");
			for (Map<String, Object> customerUpdate : customerUpdates) {
				LOGGER.debug("updating customer with '{}'", customerUpdate);
				try {
					final long id = Long.valueOf(customerUpdate.get("id").toString());
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
							LOGGER.debug("customer with id '{}' was synchronized on '{}'", id, synchronizedOn);
							return null;
						}
					});
				} catch (Exception e) {
					LOGGER.error(e.getMessage());
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
}

