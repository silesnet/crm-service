package net.snet.crm.service.mapper;

import net.snet.crm.service.bo.Draft;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DraftMapper implements ResultSetMapper<Draft> {

    @Override
    public Draft map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Draft(r.getLong("id"),
                r.getString("type"),
                r.getString("user_id"),
                r.getString("data"),
								r.getString("status"));
    }
}
