package net.snet.crm.service.resources;

import java.util.Map;

/**
 * Created by admin on 21.8.14.
 */
public interface UserService {
	Map<String, Object> authenticateUserBySessionId(String sessionId);
}
