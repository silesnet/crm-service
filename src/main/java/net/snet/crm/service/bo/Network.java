package net.snet.crm.service.bo;

public class Network {
    private long id;
		private String name;
    private String master;
    private String ssid;

    public Network(long id, String name, String master, String ssid) {
        this.id = id;
				this.name = name;
        this.master = master;
        this.ssid = ssid;
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
