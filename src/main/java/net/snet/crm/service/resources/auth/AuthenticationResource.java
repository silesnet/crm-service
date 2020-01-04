package net.snet.crm.service.resources.auth;

import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;
import net.snet.crm.domain.model.agreement.CrmRepository;
import net.snet.crm.infrastructure.persistence.jdbi.DbiCrmRepository;
import net.snet.crm.service.auth.AccessToken;
import net.snet.crm.service.auth.AuthenticatedUser;
import net.snet.crm.service.auth.SessionId;
import net.snet.crm.service.auth.AuthenticationService;

import javax.ws.rs.*;
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
      LOGGER.debug("Authenticated with token '{}'", accessToken.getAccessToken());
      return Response.ok().entity(accessToken).build();
    } catch (Exception exception) {
      throw new NotAuthorizedException("authentication failed");
    }
  }

  @GET
  @Path("/users/session")
  public Response userSession(@Auth final AuthenticatedUser user) {
    LOGGER.debug("User session for '{}'", user);
    Map<String, Object> userInfo = crmRepository.findUserByLogin(user.getLogin());
    UserSession userSession = new UserSession(
        userInfo.get("login").toString(),
        userInfo.get("full_name").toString(),
        userInfo.get("operation_country").toString(),
        userInfo.get("roles").toString().split(",\\s*")
    );
    return Response.ok().entity(userSession).build();
  }

}
