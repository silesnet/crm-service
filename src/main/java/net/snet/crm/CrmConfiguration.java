package net.snet.crm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import net.snet.crm.infrastructure.messaging.SmsMessagingConfiguration;

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

  @JsonProperty
  private String systemCommandHome;

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

  public File getSystemCommandHome() {
	  String value = systemCommandHome;
    if (value == null) {
      value = System.getProperty("sis.command.home");
    }
    if (value == null) {
      value = System.getenv("SIS_COMMAND_HOME");
    }
    return new File(value).getAbsoluteFile();
  }

  public SmsMessagingConfiguration getSmsMessaging() {
		return smsMessaging;
	}
}
