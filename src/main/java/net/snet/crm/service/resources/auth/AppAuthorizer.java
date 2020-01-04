package net.snet.crm.service.resources.auth;

import io.dropwizard.auth.Authorizer;
import net.snet.crm.service.auth.AuthenticatedUser;

import java.util.Arrays;

public class AppAuthorizer implements Authorizer<AuthenticatedUser> {
  @Override
  public boolean authorize(AuthenticatedUser principal, String role) {
    return Arrays.binarySearch(principal.getRoles(), role) > 0;
  }
}
