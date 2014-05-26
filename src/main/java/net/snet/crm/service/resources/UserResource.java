package net.snet.crm.service.resources;

import com.yammer.metrics.annotation.Timed;
import net.snet.crm.service.bo.User;
import net.snet.crm.service.dao.UserDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("/users")
public class UserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    private UserDAO userDAO;

    public UserResource(DBI dbi) {
        this.userDAO = dbi.onDemand(UserDAO.class);
    }

    @GET
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> getAllUsers() {
        LOGGER.debug("users called");

        final HashMap<String, Object> usersMap = new HashMap<String, Object>();

        Iterator<User> users = userDAO.allUsers();

        usersMap.put("users", users);

        return usersMap;
    }
}

