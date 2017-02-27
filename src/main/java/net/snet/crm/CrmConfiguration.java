package net.snet.crm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.db.DataSourceFactory;
import net.snet.crm.infrastructure.messaging.SmsMessagingConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static java.util.Arrays.asList;

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

  @JsonProperty
  private String version;

	@Valid
	@NotNull
	@JsonProperty
	private SmsMessagingConfiguration smsMessaging = new SmsMessagingConfiguration();

	@Valid
	@NotNull
	@JsonProperty
	private String addressServiceUri;

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
    final String value = Iterables.find(
        asList(getProperty("sis.command.home"), getenv("SIS_COMMAND_HOME"), systemCommandHome),
        Predicates.<String>notNull()
    );
    return new File(value).getAbsoluteFile();
  }

  public SmsMessagingConfiguration getSmsMessaging() {
		return smsMessaging;
  }

  public String getVersion() {
    return version;
  }

	public String getAddressServiceUri()
	{
		return addressServiceUri;
	}
}
