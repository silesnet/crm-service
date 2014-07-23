package net.snet.crm.service.dao;

import java.util.Map;

public interface CrmRepository {
	Map<String, Object> insertCustomer(Map<String, Object> customer);
	Map<String, Object> findCustomerById(long custoemrId);

	Map<String,Object> insertAgreement(long customerId, String country);
}
