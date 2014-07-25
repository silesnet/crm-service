package net.snet.crm.service.dao;

import java.util.Map;

public interface CrmRepository {
	Map<String, Object> insertCustomer(Map<String, Object> customer);

	Map<String, Object> findCustomerById(long customerId);

	Map<String,Object> insertAgreement(long customerId, String country);

	Map<String, Object> findAgreementById(long agreementId);

	Map<String, Object> insertService(long agreementId);

	Map<String, Object> findServiceById(long serviceId);

}
