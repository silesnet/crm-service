package net.snet.crm.infrastructure.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.snet.crm.service.auth.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

@Slf4j
public class HttpUserService implements UserService {
  private final Client httpClient;
  private final URI serviceUri;
  private final ObjectMapper mapper;

  public HttpUserService(Client httpClient, URI serviceUri) {
    this.httpClient = httpClient;
    this.serviceUri = serviceUri;
    this.mapper = new ObjectMapper();
  }

  @Override
  public AuthenticatedUser authenticate(SessionId sessionId) {
    final URI authUri = UriBuilder.fromUri(serviceUri).matrixParam("jsessionid", sessionId.getValue()).build();
    LOGGER.debug("authentication URI '{}'", authUri);
    try {
      final String response = httpClient.target(authUri).request().get(String.class);
      Map principal = mapper.readValue(response, Map.class);
      return authenticatedUser(principal);
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      throw new AuthenticationException(e);
    }
  }

  private AuthenticatedUser authenticatedUser(final Map principal) {
    String login = principal.get("user").toString();
    String[] roles = principal.get("roles").toString().split(",\\s*");
    return new AuthenticatedUser(login, roles);
  }
}
