package net.snet.crm.service.bo;

public class User {
  private long id;
  private String login;
  private String name;
  private String fullName;
  private String country;

  public User(long id, String name, String login, String fullName, String country) {
    this.id = id;
    this.login = login;
    this.name = name;
    this.fullName = fullName;
    this.country = country;
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

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String operationCountry) {
    this.country = operationCountry;
  }
}
