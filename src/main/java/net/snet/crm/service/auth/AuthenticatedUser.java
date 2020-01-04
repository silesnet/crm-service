package net.snet.crm.service.auth;

import lombok.Value;

import javax.security.auth.Subject;
import java.security.Principal;

@Value
public class AuthenticatedUser implements Principal {
  private final String login;
  private final String[] roles;

  @Override
  public String getName() {
    return login;
  }

  @Override
  public boolean implies(Subject subject) {
    return true;
  }
}
