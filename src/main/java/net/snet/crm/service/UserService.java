package net.snet.crm.service;

import java.util.Map;

/**
 * Created by admin on 21.8.14.
 */
public interface UserService {
	Map<String, Object> authenticateUserBySessionId(String sessionId);
}
