package net.snet.crm.service.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.JsonSnakeCase;

@JsonSnakeCase
public class Product {

    private long id;
    private String name;
    private int downlink;
    private int uplink;
    private int price;
    private String channel;
    private boolean isDedicated;
    private String country;

    public Product(long id, String name, int downlink, int uplink, int price, String channel,
                   boolean isDedicated, String country) {
        this.id = id;
        this.name = name;
        this.downlink = downlink;
        this.uplink = uplink;
        this.price = price;
        this.channel = channel;
        this.isDedicated = isDedicated;
        this.country = country;
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

    public int getDownlink() {
        return downlink;
    }

    public void setDownlink(int downlink) {
        this.downlink = downlink;
    }

    public int getUplink() {
        return uplink;
    }

    public void setUplink(int uplink) {
        this.uplink = uplink;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    @JsonProperty("isDedicated")
    public boolean isDedicated() {
        return isDedicated;
    }

    @JsonProperty("isDedicated")
    public void setDedicated(boolean isDedicated) {
        this.isDedicated = isDedicated;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
