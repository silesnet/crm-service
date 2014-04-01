/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snet.crm.api.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import net.snet.crm.api.dao.CustomerDao;
import net.snet.crm.api.model.Customer;
import net.snet.crm.api.model.Customers;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.IntegerMapper;
import static sun.misc.Signal.handle;
import net.snet.crm.api.dao.map.CustomerMapper;

/**
 *
 * @author chemik
 */
public class CustomerDaoJdbi implements CustomerDao {

	private List<Customer> customer;
	private final DBI jdbi;
	private Status responseCode;

	public CustomerDaoJdbi(DBI jdbi) {
		this.jdbi = jdbi;
	}

	public List<Customer> getCustomer() {
		return customer;
	}

	public void setCustomer(List<Customer> customer) {
		this.customer = customer;
	}

	@Override
	public Customers storeCustomer(Customers customers) {
		Handle handle = null;
		String sql;

		Customers customersOut = new Customers();
		customersOut.setCustomers(customers.getCustomers());

		//Only one customer per request is acceptable + check obligatory fields
		if (customersOut.size() != 1 || customersOut.checkField() == false) {
			responseCode = Response.Status.BAD_REQUEST;
			return customersOut;
		}

		try {
			handle = jdbi.open();
			handle.begin();

			customersOut.setInsertedOn(new Timestamp(System.currentTimeMillis()));
			Customer customerIn = customersOut.getCustomers().get(0);

			List<Customer> customer_db = handle.createQuery("SELECT * FROM customers WHERE public_id = :public_id or contract_no = :contract_no")
							.bind("public_id", customerIn.getPublicId())
							.bind("contract_no", customerIn.getContractNo())
							.map(new CustomerMapper()).list();

			if (!customer_db.isEmpty()) {
				responseCode = Response.Status.CONFLICT;
				customersOut.setCustomers(customer_db);
				return customersOut;
			}

			//Get max id
			long max_id = handle.createQuery("SELECT MAX(id) FROM customers").map(IntegerMapper.FIRST).first();
			max_id++;
			
			customersOut.setId(max_id);

			sql = "INSERT INTO customers (id, history_id, public_id, name, street, city, postal_code, country, email, dic, contract_no,"
					+ " connection_spot, inserted_on, frequency, lastly_billed, is_billed_after, deliver_by_email, deliver_copy_email,"
					+ " deliver_by_mail, is_auto_billing, info, contact_name, phone, is_active, status, shire_id, format, deliver_signed, symbol,"
					+ " account_no, bank_no, variable)"
					+ " VALUES (:id, :history_id, :public_id, :name, :street, :city, :postal_code, :country, :email, :dic, :contract_no,"
					+ " :connection_spot, :inserted_on, :frequency, :lastly_billed, :is_billed_after, :deliver_by_email, :deliver_copy_email,"
					+ " :deliver_by_mail, :is_auto_billing, :info, :contact_name, :phone, :is_active, :status, :shire_id, :format, :deliver_signed, :symbol,"
					+ " :account_no, :bank_no, :variable)";

			handle.createStatement(sql)
						.bind("id", max_id)
						.bind("history_id", customerIn.getHistoryId())
						.bind("public_id", customerIn.getPublicId())
						.bind("name", customerIn.getName())
						.bind("street", customerIn.getStreet())
						.bind("city", customerIn.getCity())
						.bind("postal_code", customerIn.getPostalCode())
						.bind("country", customerIn.getCountry())
						.bind("email", customerIn.getEmail())
						.bind("dic", customerIn.getDic())
						.bind("contract_no", customerIn.getContractNo())
						.bind("connection_spot", customerIn.getConnectionSpot())
						.bind("inserted_on", customerIn.getInsertedOn())
						.bind("frequency", customerIn.getFrequency())
						.bind("lastly_billed", customerIn.getLastlyBilled())
						.bind("is_billed_after", customerIn.getIsBilledAfter())
						.bind("deliver_by_email", customerIn.getDeliverByEmail())
						.bind("deliver_copy_email", customerIn.getDeliverCopyEmail())
						.bind("deliver_by_mail", customerIn.getDeliverByMail())
						.bind("is_auto_billing", customerIn.getIsAutoBilling())
						.bind("info", customerIn.getInfo())
						.bind("contact_name", customerIn.getContactName())
						.bind("phone", customerIn.getPhone())
						.bind("is_active", customerIn.getIsActive())
						.bind("status", customerIn.getStatus())
						.bind("shire_id", customerIn.getShireId())
						.bind("format", customerIn.getFormat())
						.bind("deliver_signed", customerIn.getDeliverSigned())
						.bind("symbol", customerIn.getSymbol())
						.bind("account_no", customerIn.getAccountNo())
						.bind("bank_no", customerIn.getBankNo())
						.bind("variable", customerIn.getVariable())
						.execute();

			handle.commit();
			responseCode = Response.Status.fromStatusCode(201);
			return customersOut;

		} finally {
			if (handle != null) {
				handle.close();
			}
		}
	}

	@Override
	public List<Customer> getCustomerById(int id) {

		Handle handle = null;
		List<Customer> customers = new ArrayList<Customer>();

		try {
			handle = jdbi.open();
			handle.begin();

			Customer customer_l = handle.createQuery("SELECT * FROM customers WHERE id = :id")
							.bind("id", id)
							.map(new CustomerMapper()).first();
			if (customer_l == null) {
				responseCode = Response.Status.CONFLICT;
			} else {
				responseCode = Response.Status.OK;
			}
			customers.add(customer_l);

			return customers;

		} finally {
			if (handle != null) {
				handle.close();
			}
		}
	}

    @Override
    public List<Customer> getCustomerByName(String name) {
        Handle handle = null;
        List<Customer> customers = new ArrayList<Customer>();

        String sFromChars = "ÁĄÄČĆĎÉĚĘËÍŁŇŃÓÖŘŠŚŤÚŮÜÝŽŻŹáąäčćďéěęëíłňńóöřšśťúůüýžżź.-,;:&+ ";

        String sToChars = "aaaccdeeeeilnnoorsstuuuyzzzaaaccdeeeeilnnoorsstuuuyzzz";

        try {
            handle = jdbi.open();
            handle.begin();

            Customer customer_l = handle.createQuery("SELECT * FROM customers WHERE lower(translate(name ,'" + sFromChars + "', '" + sToChars + "')) like '%" + name +"%'")
                    .map(new CustomerMapper()).first();
            if (customer_l == null) {
                responseCode = Response.Status.CONFLICT;
            } else {
                responseCode = Response.Status.OK;
            }
            customers.add(customer_l);

            return customers;

        } finally {
            if (handle != null) {
                handle.close();
            }
        }
    }
}
