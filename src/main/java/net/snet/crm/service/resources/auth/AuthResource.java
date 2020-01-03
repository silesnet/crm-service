package net.snet.crm.service.resources.auth;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import net.snet.crm.service.resources.auth.Credentials;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/api")
@Produces({"application/json; charset=UTF-8"})
@Slf4j
public class AuthResource {
  private final DBI dbi;

  public AuthResource(final DBI dbi) {
    this.dbi = dbi;
  }

  @POST
  @Path("/auth/token")
  public Response authenticationToken(final Credentials credentials) {
    LOGGER.info("Authenticating by '{}'", credentials);
    return Response.ok().entity(new AccessToken("123456789")).build();
  }

  @GET
  @Path("/users/session")
  public Response userSession() {
    LOGGER.info("Getting authenticated user session...");
    UserSession userSession = new UserSession("ikaleta", "Ivo Kaleta", "CZ", new String[]{"ROLE_USER"});
    return Response.ok().entity(userSession).build();
  }
}
