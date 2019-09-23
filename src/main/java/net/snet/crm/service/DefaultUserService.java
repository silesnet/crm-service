package net.snet.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.model.agreement.CrmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * Created by admin on 21.8.14.
 */
public class DefaultUserService implements UserService
{
	private static final Logger logger = LoggerFactory.getLogger(DefaultUserService.class);

	private final Client httpClient;
	private final URI serviceUri;
	private final CrmRepository crmRepository;

	public DefaultUserService(Client httpClient, URI serviceUri, CrmRepository crmRepository) {
		this.httpClient = httpClient;
		this.serviceUri = serviceUri;
		this.crmRepository = crmRepository;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> authenticateUserBySessionId(String sessionId) {
		final URI authUri = UriBuilder.fromUri(serviceUri).matrixParam("jsessionid", sessionId).build();
		try {
			final String response = httpClient.target(authUri).request().get(String.class);
			logger.debug("user service response '{}'", response);
			Map rawUser = new ObjectMapper().readValue(response, Map.class);
			Map<String, Object> user = crmRepository.findUserByLogin(rawUser.get("user").toString());
			user.put("user", user.get("login"));
			user.remove("login");
			return user;
		} catch (Exception e) {
			return ImmutableMap.of();
		}
	}
}
