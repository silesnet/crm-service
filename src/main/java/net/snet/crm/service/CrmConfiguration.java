package net.snet.crm.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class CrmConfiguration extends Configuration {

    @Valid
    @JsonProperty
    private Boolean jsonPrettyPrint = true;

    @Valid
    @NotNull
    @JsonProperty
    private DataSourceFactory database = new DataSourceFactory();

    public Boolean getJsonPrettyPrint() {
        return jsonPrettyPrint;
    }

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
}
