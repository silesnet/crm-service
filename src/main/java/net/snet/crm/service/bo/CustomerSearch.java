package net.snet.crm.service.bo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerSearch {
    private long id;
    private String name;
    private String street;
    private String city;
    private String postalCode;

    public CustomerSearch(long id, String name, String street, String city, String postalCode) {
        this.id = id;
        this.name = name;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
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

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty("postalCode")
    public String getPostalCode() {
        return postalCode;
    }

    @JsonProperty("postalCode")
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}

