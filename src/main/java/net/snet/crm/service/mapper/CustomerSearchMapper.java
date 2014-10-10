package net.snet.crm.service.mapper;

import net.snet.crm.service.bo.CustomerSearch;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerSearchMapper implements ResultSetMapper<CustomerSearch> {

    @Override
    public CustomerSearch map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new CustomerSearch(r.getLong("id"),
                r.getString("name"),
                r.getString("street"),
                r.getString("city"),
                r.getString("postal_code")
        );
    }
}
