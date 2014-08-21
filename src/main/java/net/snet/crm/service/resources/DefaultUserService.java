package net.snet.crm.service.resources;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.Client;

import java.util.Map;

/**
 * Created by admin on 21.8.14.
 */
public class DefaultUserService implements UserService {
	private final Client httpClient;

	public DefaultUserService(Client httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public Map<String, Object> authenticateUserBySessionId(String sessionId) {
		return ImmutableMap.of();
	}
}
