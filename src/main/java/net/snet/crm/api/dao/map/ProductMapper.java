package net.snet.crm.api.dao.map;

import net.snet.crm.api.model.Product;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by admin on 22.12.13.
 */
public class ProductMapper implements ResultSetMapper<Product> {

	@Override
	public Product map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new Product(r.getLong("id"), r.getString("name"), r.getInt("download"), r.getInt("upload"), r.getInt("price"));
	}
}
