package net.snet.crm.domain.shared.auth;

import lombok.Value;

@Value
public class User {
  private final String login;
  private final String fullName;
  private final String division;
  private final String[] roles;
}
