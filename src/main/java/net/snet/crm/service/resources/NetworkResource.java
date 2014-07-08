package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import net.snet.crm.service.bo.Network;
import net.snet.crm.service.dao.NetworkDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("/networks")
public class NetworkResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkResource.class);

    private NetworkDAO networkDAO;

    public NetworkResource(DBI dbi) {
        this.networkDAO = dbi.onDemand(NetworkDAO.class);
    }

    @GET
    @Path("/routers")
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> getAllNetworks() {
        LOGGER.debug("routers called");

        final HashMap<String, Object> coreRoutersMap = new HashMap<String, Object>();

        Iterator<Network> routers = networkDAO.allMasters();

        coreRoutersMap.put("core_routers", routers);

        return coreRoutersMap;
    }

    @GET
    @Path("/ssids")
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> getAllSsids() {
        LOGGER.debug("ssid called");

        final HashMap<String, Object> networksMap = new HashMap<String, Object>();

        Iterator<Network> networks = networkDAO.allSsids();

        networksMap.put("ssids", networks);

        return networksMap;
    }
}

