package net.snet.crm.service.resources;

import com.yammer.metrics.annotation.Timed;
import net.snet.crm.service.bo.CustomerSearch;
import net.snet.crm.service.bo.Service;
import net.snet.crm.service.dao.CustomerDAO;
import net.snet.crm.service.dao.ServiceDAO;
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
    //private Customers customers;

    public CustomerResource(DBI dbi) {
        this.customerDAO = dbi.onDemand(CustomerDAO.class);
        this.serviceDAO = dbi.onDemand(ServiceDAO.class);
    }

    private String replaceChars(String str, String searchChars, String replaceChars) {
        int replaceCharsLength = replaceChars.length();
        int strLength = str.length();
        StringBuilder buf = new StringBuilder(strLength);
        for (int i = 0; i < strLength; i++) {
            char ch = str.charAt(i);
            int index = searchChars.indexOf(ch);
            if (index >= 0) {
                if (index < replaceCharsLength) {
                    buf.append(replaceChars.charAt(index));
                }
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    @GET
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> getCustomersByQuery(@QueryParam("q") String name, @QueryParam("c") int count) {
        LOGGER.debug("customers called");

        final HashMap<String, Object> customersMap = new HashMap<>();

        Iterator<CustomerSearch> tmp_customers = customerDAO.getCustomersByName("%" + replaceChars(name, FROM_CHARS, TO_CHARS) + "%", FROM_CHARS, TO_CHARS);

        List<CustomerSearch> customersSearchList = new ArrayList<CustomerSearch>();
        while (tmp_customers.hasNext()) {
            customersSearchList.add(tmp_customers.next());
        }

        for (CustomerSearch customerSearch : customersSearchList) {
            customerSearch.setContracts(getContracts(customerSearch.getId()));
        }

        customersMap.put("customers", customersSearchList);

        return customersMap;
    }

    /**
     * Return an ArrayList with BillItems
     *
     * @param id Bill Id
     * @return ArrayList with BillItems
     */
    private ArrayList<Long> getContracts(long id) {
        ArrayList<Long> retContracts = new ArrayList<Long>();

        Iterator<Service> contracts = serviceDAO.findContractsByCustomerId(id);

        while (contracts.hasNext()) {
            retContracts.add(contracts.next().getId());
        }
        return retContracts;
    }
}

