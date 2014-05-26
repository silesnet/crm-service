package net.snet.crm.service.bo;

public class Network {
    private long id;
    private String master;
    private String ssid;

    public Network(long id, String name, String ssid) {
        this.id = id;
        this.master = name;
        this.ssid = ssid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }
}
