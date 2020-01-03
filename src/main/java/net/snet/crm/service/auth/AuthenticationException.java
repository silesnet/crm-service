package net.snet.crm.service.auth;

public class AuthenticationException extends RuntimeException {
  public AuthenticationException() {
  }

  public AuthenticationException(Exception e) {
    super(e);
  }
}
