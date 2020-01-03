package net.snet.crm.service.auth;

public interface UserService {
  AuthenticatedUser authenticate(SessionId sessionId);
}
