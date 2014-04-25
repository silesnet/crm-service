package net.snet.crm.service.mapper;

import net.snet.crm.service.bo.Product;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMapper implements ResultSetMapper<Product> {

    @Override
    public Product map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Product(r.getLong("id"),
                r.getString("name"),
                r.getInt("downlink"),
                r.getInt("uplink"),
                r.getInt("price"),
                r.getString("channel"),
                r.getBoolean("is_dedicated"));
    }
}
