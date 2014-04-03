package net.snet.crm.service.resources;

import com.yammer.metrics.annotation.Timed;
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
    @HeaderParam("Access-Control-Allow-Origin=*;")
    @Timed(name = "get-requests")
    public Map<String, Object> getCustomersByQuery(@QueryParam("q") String name, @QueryParam("c") int count) {
        LOGGER.debug("customers called");

        final HashMap<String, Object> customersMap = new HashMap<>();

        Iterator<CustomerSearch> tmp_customers = customerDAO.getCustomersByName("%" + Utils.replaceChars(name, FROM_CHARS, TO_CHARS) + "%", FROM_CHARS, TO_CHARS);

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
     * Return an ArrayList with Contracts
     *
     * @param id Customer Id
     * @return ArrayList with Contracts
     */
    private ArrayList<String> getContracts(long id) {
        ArrayList<String> retContracts = new ArrayList<String>();

        Iterator<Service> contracts = serviceDAO.findContractsByCustomerId(id);

        while (contracts.hasNext()) {
            ServiceId serviceId = ServiceId.serviceId((int) contracts.next().getId());
            String contractCode = serviceId.country().getShortName().toUpperCase() + serviceId.contractNo().toString();
            boolean contractExist = false;
            for (String contract : retContracts) {
                if (contract.equals(contractCode)) {
                    contractExist = true;
                }
            }
            if (!contractExist) {
                retContracts.add(contractCode);
            }
        }
        return retContracts;
    }
}

