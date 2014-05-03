package net.snet.crm.service.resources;

import com.yammer.metrics.annotation.Timed;
import net.snet.crm.service.bo.Service;
import net.snet.crm.service.bo.ServiceId;
import net.snet.crm.service.dao.ServiceDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("/contracts")
public class ContractResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractResource.class);

    private ServiceDAO serviceDAO;

    public ContractResource(DBI dbi) {
        this.serviceDAO = dbi.onDemand(ServiceDAO.class);
    }

    @GET
    @Produces({"application/json; charset=UTF-8"})
    @Path("/{customerId}")
    @Timed(name = "get-requests")
    public Map<String, Object> findAllContractsByCustomerId(@PathParam("customerId") long customerId) {
        LOGGER.debug("contract called");

        final HashMap<String, Object> contractsMap = new HashMap<String, Object>();

        Iterator<Service> contracts = serviceDAO.findContractsByCustomerId(customerId);

        ArrayList<Integer> retContracts = new ArrayList<Integer>();

        while (contracts.hasNext()) {

            ServiceId serviceId = ServiceId.serviceId((int) contracts.next().getId());
            boolean contractExist = false;
            for (int contractId : retContracts) {
                if (contractId == serviceId.contractNo().value()) {
                    contractExist = true;
                }
            }
            if (!contractExist) {
                retContracts.add(serviceId.contractNo().value());
            }
        }

        contractsMap.put("contracts", retContracts);

        return contractsMap;
    }
}

