package net.snet.crm.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CrmConfiguration extends Configuration {

    @Valid
    @JsonProperty
    private Boolean jsonPrettyPrint = false;

    @Valid
    @NotNull
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    public Boolean getJsonPrettyPrint() {
        return jsonPrettyPrint;
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        return database;
    }
}
