package net.snet.crm.service.mapper;

import net.snet.crm.service.bo.Network;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NetworkMapper implements ResultSetMapper<Network> {

    @Override
    public Network map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Network(r.getLong("id"),
								r.getString("name"),
                r.getString("master"),
                r.getString("ssid"));
    }
}
