package net.snet.crm.domain.shared.auth;

import java.util.Optional;

public interface UserRepository {
  Optional<User> fetchByLogin(String login);
}
