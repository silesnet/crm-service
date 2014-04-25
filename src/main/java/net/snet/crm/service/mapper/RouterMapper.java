package net.snet.crm.service.mapper;

import net.snet.crm.service.bo.Router;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RouterMapper implements ResultSetMapper<Router> {

    @Override
    public Router map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Router(r.getLong("id"),
                r.getString("name"));
    }
}
