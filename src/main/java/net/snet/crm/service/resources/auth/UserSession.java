package net.snet.crm.service.resources.auth;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.snet.crm.domain.shared.auth.User;

@Value
@RequiredArgsConstructor
public class UserSession {
  private final String login;
  private final String fullName;
  private final String division;
  private final String[] roles;

  public UserSession(User user) {
    this(user.getLogin(), user.getFullName(), user.getDivision(), user.getRoles());
  }
}
