/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.snet.crm.api.dao;

import java.util.List;
import javax.ws.rs.core.Response.Status;
import net.snet.crm.api.model.Customer;
import net.snet.crm.api.model.Customers;

/**
 *
 * @author chemik
 */
public interface CustomerDao {

	public Customers storeCustomer(Customers customers);

	public List<Customer> getCustomerById(int id);

    public List<Customer> getCustomerByName(String name);

}
