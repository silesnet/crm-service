package net.snet.crm.service.resources;

import com.yammer.metrics.annotation.Timed;
import net.snet.crm.service.bo.Router;
import net.snet.crm.service.dao.RouterDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("/routers")
public class RouterResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouterResource.class);

    private RouterDAO routerDAO;

    public RouterResource(DBI dbi) {
        this.routerDAO = dbi.onDemand(RouterDAO.class);
    }

    @GET
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> getAllRouters() {
        LOGGER.debug("routers called");

        final HashMap<String, Object> routersMap = new HashMap<>();

        Iterator<Router> routers = routerDAO.allRouters();

        routersMap.put("core_routers", routers);

        return routersMap;
    }
}

