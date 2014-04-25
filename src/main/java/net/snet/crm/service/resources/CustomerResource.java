package net.snet.crm.service.resources;

import com.yammer.metrics.annotation.Timed;
import net.snet.crm.service.bo.Customer;
import net.snet.crm.service.bo.CustomerSearch;
import net.snet.crm.service.bo.Service;
import net.snet.crm.service.bo.ServiceId;
import net.snet.crm.service.dao.CustomerDAO;
import net.snet.crm.service.dao.ServiceDAO;
import net.snet.crm.service.utils.Utils;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerResource.class);

    private final String FROM_CHARS = "ÁĄÄČĆĎÉĚĘËÍŁŇŃÓÖŘŠŚŤÚŮÜÝŽŻŹáąäčćďéěęëíłňńóöřšśťúůüýžżź.-,;:&+? ";
    private final String TO_CHARS = "aaaccdeeeeilnnoorsstuuuyzzzaaaccdeeeeilnnoorsstuuuyzzz";

    private CustomerDAO customerDAO;
    private ServiceDAO serviceDAO;

    public CustomerResource(DBI dbi) {
        this.customerDAO = dbi.onDemand(CustomerDAO.class);
        this.serviceDAO = dbi.onDemand(ServiceDAO.class);
    }

    @GET
    @Produces({"application/json; charset=UTF-8"})
    @Path("/{customerId}")
    @Timed(name = "get-requests")
    public Map<String, Object> getCustomerById(@PathParam("customerId") long id) {
        LOGGER.debug("customers called");

        final HashMap<String, Object> customersMap = new HashMap<>();

        customersMap.put("customer", customerDAO.findById(id));

        return customersMap;
    }

    @GET
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> getCustomersByQuery(@QueryParam("q") String name, @QueryParam("c") int count) {
        LOGGER.debug("customers called");

        final HashMap<String, Object> customersMap = new HashMap<>();

        Iterator<CustomerSearch> customers = customerDAO.getCustomersByName("%" + Utils.replaceChars(name, FROM_CHARS, TO_CHARS) + "%", FROM_CHARS, TO_CHARS);

        List<CustomerSearch> retCustomers = new ArrayList<CustomerSearch>();
        
        while (customers.hasNext()) {
            retCustomers.add(customers.next());
        }
        customersMap.put("customers", retCustomers);

        return customersMap;
    }
}

