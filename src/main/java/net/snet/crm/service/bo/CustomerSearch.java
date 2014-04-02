package net.snet.crm.service.bo;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.json.JsonSnakeCase;

import java.util.List;


@JsonSnakeCase
public class CustomerSearch {
    private long id;
    @JsonProperty
    private String name;
    @JsonProperty
    private String supplementaryName;
    private List<Long> contracts;

    public CustomerSearch(long id, String name, String supplementaryName) {
        this.id = id;
        this.name = name;
        this.supplementaryName = supplementaryName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSupplementaryName() {
        return supplementaryName;
    }

    public void setSupplementaryName(String supplementaryName) {
        this.supplementaryName = supplementaryName;
    }

    public List<Long> getContracts() {
        return contracts;
    }

    public void setContracts(List<Long> contracts) {
        this.contracts = contracts;
    }

}

