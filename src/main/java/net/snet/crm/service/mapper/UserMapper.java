package net.snet.crm.service.mapper;

import net.snet.crm.service.bo.User;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements ResultSetMapper<User> {

  @Override
  public User map(int index, ResultSet r, StatementContext ctx) throws SQLException {
    return new User(
        r.getLong("id"),
        r.getString("name"),
        r.getString("login"),
        r.getString("full_name"));
  }
}
