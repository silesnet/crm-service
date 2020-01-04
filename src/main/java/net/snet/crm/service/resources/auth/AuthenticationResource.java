package net.snet.crm.service.resources.auth;

import lombok.extern.slf4j.Slf4j;
import net.snet.crm.domain.model.agreement.CrmRepository;
import net.snet.crm.infrastructure.persistence.jdbi.DbiCrmRepository;
import net.snet.crm.service.auth.AccessToken;
import net.snet.crm.service.auth.AuthenticatedUser;
import net.snet.crm.service.auth.SessionId;
import net.snet.crm.service.auth.AuthenticationService;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/api")
@Produces({"application/json; charset=UTF-8"})
@Slf4j
public class AuthenticationResource {
  private final AuthenticationService authenticationService;
  private final CrmRepository crmRepository;

  public AuthenticationResource(AuthenticationService authenticationService, DbiCrmRepository crmRepository) {
    this.authenticationService = authenticationService;
    this.crmRepository = crmRepository;
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
  public Response userSession(@Context final HttpHeaders headers) {
    try {
      final AccessToken accessToken = new AccessToken(headers.getHeaderString("authorization").split(" ")[1]);
      LOGGER.info("Getting user session by access token '{}'", accessToken);
      AuthenticatedUser authenticatedUser = authenticationService.authenticate(accessToken);
      Map<String, Object> userInfo = crmRepository.findUserByLogin(authenticatedUser.getLogin());
      UserSession userSession = new UserSession(
          userInfo.get("login").toString(),
          userInfo.get("full_name").toString(),
          userInfo.get("operation_country").toString(),
          userInfo.get("roles").toString().split(",\\s*")
      );
      return Response.ok().entity(userSession).build();
    } catch (Exception exception) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
  }

}
