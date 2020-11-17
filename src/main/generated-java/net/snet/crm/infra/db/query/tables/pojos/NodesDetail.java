/*
 * This file is generated by jOOQ.
 */
package net.snet.crm.infra.db.query.tables.pojos;


import java.io.Serializable;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class NodesDetail implements Serializable {

    private static final long serialVersionUID = -1586337302;

    private final Integer id;
    private final String  country;
    private final String  name;
    private final String  type;
    private final String  master;
    private final String  linkTo;
    private final String  area;
    private final String  vendor;
    private final String  model;
    private final String  info;
    private final String  monitoring;
    private final String  path;
    private final String  ping;
    private final Boolean isWireless;
    private final String  polarization;
    private final String  width;
    private final String  norm;
    private final Boolean tdma;
    private final Boolean aggregation;
    private final String  ssid;
    private final Integer frequency;
    private final String  power;
    private final String  antenna;
    private final Boolean wds;
    private final String  authentication;
    private final String  azimuth;
    private final Boolean active;

    public NodesDetail(NodesDetail value) {
        this.id = value.id;
        this.country = value.country;
        this.name = value.name;
        this.type = value.type;
        this.master = value.master;
        this.linkTo = value.linkTo;
        this.area = value.area;
        this.vendor = value.vendor;
        this.model = value.model;
        this.info = value.info;
        this.monitoring = value.monitoring;
        this.path = value.path;
        this.ping = value.ping;
        this.isWireless = value.isWireless;
        this.polarization = value.polarization;
        this.width = value.width;
        this.norm = value.norm;
        this.tdma = value.tdma;
        this.aggregation = value.aggregation;
        this.ssid = value.ssid;
        this.frequency = value.frequency;
        this.power = value.power;
        this.antenna = value.antenna;
        this.wds = value.wds;
        this.authentication = value.authentication;
        this.azimuth = value.azimuth;
        this.active = value.active;
    }

    public NodesDetail(
        Integer id,
        String  country,
        String  name,
        String  type,
        String  master,
        String  linkTo,
        String  area,
        String  vendor,
        String  model,
        String  info,
        String  monitoring,
        String  path,
        String  ping,
        Boolean isWireless,
        String  polarization,
        String  width,
        String  norm,
        Boolean tdma,
        Boolean aggregation,
        String  ssid,
        Integer frequency,
        String  power,
        String  antenna,
        Boolean wds,
        String  authentication,
        String  azimuth,
        Boolean active
    ) {
        this.id = id;
        this.country = country;
        this.name = name;
        this.type = type;
        this.master = master;
        this.linkTo = linkTo;
        this.area = area;
        this.vendor = vendor;
        this.model = model;
        this.info = info;
        this.monitoring = monitoring;
        this.path = path;
        this.ping = ping;
        this.isWireless = isWireless;
        this.polarization = polarization;
        this.width = width;
        this.norm = norm;
        this.tdma = tdma;
        this.aggregation = aggregation;
        this.ssid = ssid;
        this.frequency = frequency;
        this.power = power;
        this.antenna = antenna;
        this.wds = wds;
        this.authentication = authentication;
        this.azimuth = azimuth;
        this.active = active;
    }

    public Integer getId() {
        return this.id;
    }

    public String getCountry() {
        return this.country;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getMaster() {
        return this.master;
    }

    public String getLinkTo() {
        return this.linkTo;
    }

    public String getArea() {
        return this.area;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getModel() {
        return this.model;
    }

    public String getInfo() {
        return this.info;
    }

    public String getMonitoring() {
        return this.monitoring;
    }

    public String getPath() {
        return this.path;
    }

    public String getPing() {
        return this.ping;
    }

    public Boolean getIsWireless() {
        return this.isWireless;
    }

    public String getPolarization() {
        return this.polarization;
    }

    public String getWidth() {
        return this.width;
    }

    public String getNorm() {
        return this.norm;
    }

    public Boolean getTdma() {
        return this.tdma;
    }

    public Boolean getAggregation() {
        return this.aggregation;
    }

    public String getSsid() {
        return this.ssid;
    }

    public Integer getFrequency() {
        return this.frequency;
    }

    public String getPower() {
        return this.power;
    }

    public String getAntenna() {
        return this.antenna;
    }

    public Boolean getWds() {
        return this.wds;
    }

    public String getAuthentication() {
        return this.authentication;
    }

    public String getAzimuth() {
        return this.azimuth;
    }

    public Boolean getActive() {
        return this.active;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NodesDetail (");

        sb.append(id);
        sb.append(", ").append(country);
        sb.append(", ").append(name);
        sb.append(", ").append(type);
        sb.append(", ").append(master);
        sb.append(", ").append(linkTo);
        sb.append(", ").append(area);
        sb.append(", ").append(vendor);
        sb.append(", ").append(model);
        sb.append(", ").append(info);
        sb.append(", ").append(monitoring);
        sb.append(", ").append(path);
        sb.append(", ").append(ping);
        sb.append(", ").append(isWireless);
        sb.append(", ").append(polarization);
        sb.append(", ").append(width);
        sb.append(", ").append(norm);
        sb.append(", ").append(tdma);
        sb.append(", ").append(aggregation);
        sb.append(", ").append(ssid);
        sb.append(", ").append(frequency);
        sb.append(", ").append(power);
        sb.append(", ").append(antenna);
        sb.append(", ").append(wds);
        sb.append(", ").append(authentication);
        sb.append(", ").append(azimuth);
        sb.append(", ").append(active);

        sb.append(")");
        return sb.toString();
    }
}
