package net.snet.crm.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
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
	private String pppoeKickUserCommand;

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

  public String getPppoeKickUserCommand() {
    return pppoeKickUserCommand;
  }
}
