package net.snet.crm.service.resources.auth;

import lombok.extern.slf4j.Slf4j;
import net.snet.crm.service.auth.AccessToken;
import net.snet.crm.service.auth.SessionId;
import net.snet.crm.service.auth.AuthenticationService;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/api")
@Produces({"application/json; charset=UTF-8"})
@Slf4j
public class AuthenticationResource {
  private final AuthenticationService authenticationService;

  public AuthenticationResource(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @POST
  @Path("/auth/token")
  public Response authenticationToken(final Credentials credentials) {
    LOGGER.info("Authenticating by '{}'", credentials);
    try {
      final AccessToken accessToken = authenticationService.authenticate(new SessionId(credentials.getSessionId()));
      return Response.ok().entity(accessToken).build();
    } catch (Exception exception) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
  }

  @GET
  @Path("/users/session")
  public Response userSession() {
    LOGGER.info("Getting authenticated user session...");
    UserSession userSession = new UserSession("ikaleta", "Ivo Kaleta", "CZ", new String[]{"ROLE_USER"});
    return Response.ok().entity(userSession).build();
  }

}
