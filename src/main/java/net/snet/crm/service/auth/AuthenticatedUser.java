package net.snet.crm.service.auth;

import lombok.Value;

@Value
public class AuthenticatedUser {
  private final String login;
  private final String[] roles;
}
