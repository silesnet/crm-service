package net.snet.crm.service.resources.auth;

import lombok.Value;

@Value
public class UserSession {
  private final String login;
  private final String fullName;
  private final String division;
  private final String[] roles;
}
