package net.snet.crm.api.model;

import java.sql.Timestamp;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author chemik
 */
public class Customers {

	private List<Customer> customers;

	public List<Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(List<Customer> customers) {
		this.customers = customers;
	}

	public void setInsertedOn(Timestamp insertedOn) {
		Customer customer = this.customers.get(0);
		customer.setInsertedOn(insertedOn);
	}

	public void setId(long id) {
		Customer customer = this.customers.get(0);
		customer.setId(id);
	}

	public boolean checkField() {
		Customer customer = this.customers.get(0);
		//Check obligatory fields
		if (customer.getName().isEmpty() || customer.getPublicId().isEmpty() || customer.getStreet().isEmpty()
						|| customer.getCity().isEmpty() || customer.getPostalCode().isEmpty() || customer.getCountry() == 0
						|| customer.getEmail().isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public int size() {
		return customers.size();
	}
}
