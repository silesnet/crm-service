package net.snet.crm.service.dao;

import java.util.List;
import java.util.Map;

public interface CrmRepository {

    // customer

    Map<String, Object> insertCustomer(Map<String, Object> customer);

    Map<String, Object> findCustomerById(long customerId);

    void deleteCustomer(long customerId);

    Map<String, Object> updateCustomer(long customerId, Map<String, Object> updates);

    void setCustomerAgreements(long customerId, String agreements);

    // agreement

    Map<String, Object> insertAgreement(long customerId, String country);

    Map<String, Object> findAgreementById(long agreementId);

    List<Map<String, Object>> findAgreementsByCustomerId(long customerId);

    Map<String, Object> updateAgreementStatus(long agreementId, String status);

    // service

    Map<String, Object> insertService(long agreementId);

    Map<String, Object> findServiceById(long serviceId);

    List<Map<String, Object>> findService(String query, String country);

    void deleteService(long serviceId);

    Map<String, Object> updateService(long serviceId, Map<String, Object> updates);

    Map<String, Object> serviceDhcp(long serviceId);

    Map<String, Object> servicePppoe(long serviceId);

    void deleteDhcp(long serviceId);

    // connection

    Map<String, Object> insertConnection(long serviceId);

    Map<String, Object> findConnectionByServiceId(long serviceId);

    Map<String, Object> updateConnection(long serviceId, Iterable<Map.Entry<String, Object>> updates);

    void deleteConnection(long serviceId);

    // user

    List<Map<String, Object>> findUserSubordinates(String login);

    Map<String, Object> findUserByLogin(String login);
}
