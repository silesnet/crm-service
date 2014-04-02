package net.snet.crm.service.mapper;

import net.snet.crm.service.bo.Service;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ServiceMapper implements ResultSetMapper<Service> {

    @Override
    public Service map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Service(r.getLong("id"));
    }
}
