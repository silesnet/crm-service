package net.snet.crm;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import net.snet.crm.infrastructure.messaging.SmsMessagingConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;

public class CrmConfiguration extends Configuration {

	@Valid
	@JsonProperty
	private Boolean jsonPrettyPrint = true;

	@Valid
	@NotNull
	@JsonProperty
	private DataSourceFactory database = new DataSourceFactory();

	@Valid
	@JsonProperty
	private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

	@Valid
	@NotNull
	@JsonProperty
	private String userServiceUri;

	@Valid
	@NotNull
	@JsonProperty
	private String kickPppoeUserCommand;

	@Valid
	@NotNull
	@JsonProperty
	private String configureDhcpPortCommand;

	@Valid
	@NotNull
	@JsonProperty
	private String sendEmailCommand;

	@Valid
	@NotNull
	@JsonProperty
	private SmsMessagingConfiguration smsMessaging = new SmsMessagingConfiguration();

	public Boolean getJsonPrettyPrint() {
		return jsonPrettyPrint;
	}

	public DataSourceFactory getDataSourceFactory() {
		return database;
	}

	public JerseyClientConfiguration getHttpClientConfiguration() {
		return httpClient;
	}

	public URI getUserServiceUri() throws URISyntaxException {
		return new URI(userServiceUri);
	}

  public String getKickPppoeUserCommand() {
    return kickPppoeUserCommand;
  }

  public String getConfigureDhcpPortCommand() {
    return configureDhcpPortCommand;
  }

  public String getSendEmailCommand() {
    return sendEmailCommand;
  }

  public SmsMessagingConfiguration getSmsMessaging() {
		return smsMessaging;
	}
}
