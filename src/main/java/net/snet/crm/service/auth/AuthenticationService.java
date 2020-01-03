package net.snet.crm.service.auth;

public interface AuthenticationService {
  AccessToken authenticate(SessionId sessionId);

  AuthenticatedUser authenticate(AccessToken accessToken);
}
