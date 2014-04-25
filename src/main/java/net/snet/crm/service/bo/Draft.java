package net.snet.crm.service.bo;

import com.yammer.dropwizard.json.JsonSnakeCase;

@JsonSnakeCase
public class Draft {

    private long id;
    private String type;
    private long userId;
    private String data;

    public Draft(long id, String data) {
        this.id = id;
        this.data = data;
    }

    public Draft(String type, long userId, String data) {
        this.type = type;
        this.userId = userId;
        this.data = data;
    }

    public Draft(long id, String type, long userId, String data) {
        this.id = id;
        this.type = type;
        this.userId = userId;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
