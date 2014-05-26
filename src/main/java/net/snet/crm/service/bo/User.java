package net.snet.crm.service.bo;

public class User {
    private long id;
    private String login;
    private String name;

    public User(long id, String name, String login) {
        this.id = id;
        this.login = login;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
