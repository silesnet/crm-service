package net.snet.crm.service.dao;

import java.util.Map;

public interface CrmRepository {
	Map<String, Object> insertCustomer(Map<String, Object> customer);

	Map<String, Object> findCustomerById(long customerId);

	void deleteCustomer(long customerId);

	Map<String,Object> insertAgreement(long customerId, String country);

	Map<String, Object> findAgreementById(long agreementId);

	Map<String, Object> updateAgreementStatus(long agreementId, String status);

	Map<String, Object> insertService(long agreementId);

	Map<String, Object> findServiceById(long serviceId);

	void deleteService(long serviceId);

	Map<String,Object> insertConnection(long serviceId);

	Map<String,Object> findConnectionByServiceId(long serviceId);

	Map<String, Object> updateConnection(long serviceId, Iterable<Map.Entry<String, Object>> updates);

	void deleteConnection(long serviceId);
}
