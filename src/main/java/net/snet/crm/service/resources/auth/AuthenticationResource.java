package net.snet.crm.service.resources.auth;

import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;
import net.snet.crm.domain.shared.auth.User;
import net.snet.crm.domain.shared.auth.UserRepository;
import net.snet.crm.service.auth.AccessToken;
import net.snet.crm.service.auth.AuthenticatedUser;
import net.snet.crm.service.auth.SessionId;
import net.snet.crm.service.auth.AuthenticationService;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/api")
@Produces({"application/json; charset=UTF-8"})
@Slf4j
public class AuthenticationResource {
  private final AuthenticationService authenticationService;
  private final UserRepository userRepository;

  public AuthenticationResource(AuthenticationService authenticationService, UserRepository userRepository) {
    this.authenticationService = authenticationService;
    this.userRepository = userRepository;
  }

  @POST
  @Path("/auth/token")
  public Response authenticationToken(final Credentials credentials) {
    try {
      final AccessToken accessToken = authenticationService.authenticate(new SessionId(credentials.getSessionId()));
      return Response.ok().entity(accessToken).build();
    } catch (Exception exception) {
      throw new NotAuthorizedException("authentication failed");
    }
  }

  @GET
  @Path("/users/session")
  @PermitAll
  public Response userSession(@Auth final AuthenticatedUser principal) {
    final Optional<User> user = userRepository.fetchByLogin(principal.getLogin());
    user.orElseThrow(() -> new RuntimeException("user not found: '" + principal.getLogin() + "'"));
    return Response.ok().entity(user.map(UserSession::new)).build();
  }

}
