package net.snet.crm.service.dao;

import java.util.Map;

public interface CustomerRepository {
	Map<String, Object> insert(Map<String, Object> customer);
}
