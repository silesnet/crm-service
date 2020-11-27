package net.snet.network.shared;

import io.dropwizard.auth.*;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;
import net.snet.crm.service.auth.AuthenticatedUser;

import java.util.Optional;

public class ResourceTestHelper {
  public static ResourceTestRule.Builder resourceTestRuleBuilder() {
    return ResourceTestRule.builder()
        .addProvider(
            new AuthDynamicFeature(
                new OAuthCredentialAuthFilter.Builder<AuthenticatedUser>()
                    .setAuthenticator(credentials -> Optional.of(new AuthenticatedUser("test", arrayOf("all"))))
                    .setAuthorizer((principal, role) -> true)
                    .setPrefix("Bearer")
                    .buildAuthFilter())
        )
        .addProvider(new AuthValueFactoryProvider.Binder(AuthenticatedUser.class));

  }

  private static String[] arrayOf(String... items) {
    return items;
  }
}
