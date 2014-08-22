package net.snet.crm.service.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * Created by admin on 21.8.14.
 */
public class DefaultUserService implements UserService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserService.class);

	private final Client httpClient;
	private final URI serviceUri;

	public DefaultUserService(Client httpClient, URI serviceUri) {
		this.httpClient = httpClient;
		this.serviceUri = serviceUri;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> authenticateUserBySessionId(String sessionId) {
		final URI authUri = UriBuilder.fromUri(serviceUri).matrixParam("jsessionid", sessionId).build();
		try {
			final String response = httpClient.resource(authUri).get(String.class);
			LOGGER.debug("user service response '{}'", response);
			return new ObjectMapper().readValue(response, Map.class);
		} catch (Exception e) {
			return ImmutableMap.of();
		}
	}
}
