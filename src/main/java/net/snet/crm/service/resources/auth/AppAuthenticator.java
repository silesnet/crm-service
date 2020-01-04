package net.snet.crm.service.resources.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import lombok.extern.slf4j.Slf4j;
import net.snet.crm.service.auth.AccessToken;
import net.snet.crm.service.auth.AuthenticatedUser;
import net.snet.crm.service.auth.AuthenticationService;

import java.util.Optional;

@Slf4j
public class AppAuthenticator implements Authenticator<String, AuthenticatedUser> {
  private final AuthenticationService authenticationService;

  public AppAuthenticator(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @Override
  public Optional<AuthenticatedUser> authenticate(String credentials) throws AuthenticationException {
    LOGGER.debug("Authenticating by '{}'", credentials);
    try {
      AuthenticatedUser user = authenticationService.authenticate(new AccessToken(credentials));
      LOGGER.debug("Authenticated user '{}'", user);
      return Optional.of(user);
    } catch (Exception exception) {
      return Optional.empty();
    }
  }
}
