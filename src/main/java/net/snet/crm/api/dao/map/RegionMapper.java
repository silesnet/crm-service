package net.snet.crm.api.dao.map;

import net.snet.crm.api.model.Region;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by admin on 22.12.13.
 */
public class RegionMapper implements ResultSetMapper<Region> {

	@Override
	public Region map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new Region(r.getLong("id"), r.getString("name"));
	}
}
