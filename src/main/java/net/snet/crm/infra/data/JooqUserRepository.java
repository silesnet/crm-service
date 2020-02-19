package net.snet.crm.infra.data;

import net.snet.crm.domain.shared.auth.User;
import net.snet.crm.domain.shared.auth.UserRepository;
import net.snet.crm.infra.db.command.tables.Users;
import net.snet.crm.infra.db.command.tables.records.UsersRecord;
import org.jooq.DSLContext;

import java.util.Optional;

public class JooqUserRepository implements UserRepository {
  private final DSLContext db;

  public JooqUserRepository(DSLContext db) {
    this.db = db;
  }

  @Override
  public Optional<User> fetchByLogin(String login) {
    return db.selectFrom(Users.USERS)
        .where(Users.USERS.LOGIN.eq(login))
        .fetchOptionalInto(UsersRecord.class)
        .map(record -> new User(
            record.getLogin(),
            record.getFullName(),
            record.getOperationCountry(),
            record.getRoles().split(",\\s*")
        ));
  }
}
