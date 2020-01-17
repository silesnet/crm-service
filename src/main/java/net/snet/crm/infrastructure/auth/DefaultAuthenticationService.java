package net.snet.crm.infrastructure.auth;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.snet.crm.service.auth.*;

import java.util.UUID;

public class DefaultAuthenticationService implements AuthenticationService {
  private final static AuthenticatedUser TEST_USER = new AuthenticatedUser("test", new String[] {});
  private final static SessionId TEST_SESSION_ID = new SessionId("TEST_SESSION_ID");
  private final static AccessToken TEST_ACCESS_TOKEN  = new AccessToken("TEST_ACCESS_TOKEN");

  private final UserService userService;

  private final BiMap<AccessToken, AuthenticatedUser> users;

  public DefaultAuthenticationService(UserService userService) {
    this.userService = userService;
    this.users = HashBiMap.create();
    users.put(TEST_ACCESS_TOKEN, TEST_USER);
  }

  @Override
  public AccessToken authenticate(SessionId sessionId) {
    if (TEST_SESSION_ID.equals(sessionId)) {
      return TEST_ACCESS_TOKEN;
    }
    AuthenticatedUser user = userService.authenticate(sessionId);
    return issueAccessToken(user);
  }

  @Override
  public AuthenticatedUser authenticate(AccessToken accessToken) {
    if (!users.containsKey(accessToken)) {
      throw new AuthenticationException();
    }
    return users.get(accessToken);
  }

  private AccessToken issueAccessToken(final AuthenticatedUser user) {
    if (users.inverse().containsKey(user)) {
      users.inverse().remove(user);
    }
    AccessToken accessToken = new AccessToken(UUID.randomUUID().toString());
    users.put(accessToken, user);
    return accessToken;
  }
}
