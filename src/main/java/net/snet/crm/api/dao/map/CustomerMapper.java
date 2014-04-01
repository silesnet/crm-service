/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snet.crm.api.dao.map;

import net.snet.crm.api.model.Customer;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author chemik
 */
public class CustomerMapper implements ResultSetMapper<Customer> {

	@Override
	public Customer map(int index, ResultSet r, StatementContext ctx) throws SQLException {
		return new Customer(r.getLong("id"), r.getLong("history_id"), r.getString("public_id"), r.getString("name"),
						r.getString("supplementary_name"), r.getString("street"), r.getString("city"), r.getString("postal_code"),
						r.getInt("country"), r.getString("email"), r.getString("dic"), r.getString("contract_no"),
						r.getString("connection_spot"), r.getTimestamp("inserted_on"), r.getInt("frequency"), r.getTimestamp("lastly_billed"),
						r.getBoolean("is_billed_after"), r.getBoolean("deliver_by_email"), r.getString("deliver_copy_email"),
						r.getBoolean("deliver_by_mail"), r.getBoolean("is_auto_billing"), r.getString("info"), r.getString("contact_name"),
						r.getString("phone"), r.getBoolean("is_active"), r.getInt("status"), r.getLong("shire_id"),
						r.getInt("format"), r.getBoolean("deliver_signed"), r.getString("symbol"), r.getTimestamp("updated"),
						r.getString("account_no"), r.getString("bank_no"), r.getInt("variable"));
	}
}
