package net.snet.crm.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CrmApiConfiguration extends Configuration {

	@JsonProperty
	private String defaultName = "Anonymous";

	@Valid
	@NotNull
	@JsonProperty
	private DatabaseConfiguration database = new DatabaseConfiguration();

	public String getDefaultName() {
		return defaultName;
	}

	public DatabaseConfiguration getDatabaseConfiguration() {
		return database;
	}
}
