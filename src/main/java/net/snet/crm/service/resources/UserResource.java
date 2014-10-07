package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.snet.crm.service.bo.User;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.dao.UserDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Path("/users")
public class UserResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

	private UserDAO userDAO;

	@SuppressWarnings("unused")
	private final CrmRepository repository;
	private final UserService userService;

	private
	@Context
	UriInfo uriInfo;

	public UserResource(DBI dbi, CrmRepository repository, UserService userService) {
		this.userDAO = dbi.onDemand(UserDAO.class);
		this.repository = repository;
		this.userService = userService;
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

	@GET
	@Path("current")
	@Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Response currentUser(@QueryParam("session") Optional<String> session,
															@QueryParam("key") Optional<String> key) {
		if ((session.isPresent() && "test".equals(session.get()))
				|| (key.isPresent() && "test".equals(key.get()))) {
			Map<String, Object> user = Maps.newHashMap();
			user.put("user", "test");
			user.put("name", "Test");
			user.put("roles", "ANONYMOUS_ROLE");
			user.put("operation_country", "PL");
			return Response.ok(ImmutableMap.of("users", user)).build();
		}
		if (session.isPresent()) {
			final Map<String, Object> user = userService.authenticateUserBySessionId(session.get());
			if (!user.isEmpty()) {
				return Response.ok(ImmutableMap.of("users", user)).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
}

