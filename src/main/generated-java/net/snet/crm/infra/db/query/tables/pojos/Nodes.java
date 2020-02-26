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
public class Nodes implements Serializable {

    private static final long serialVersionUID = 514155102;

    private final Integer id;
    private final String  name;
    private final String  master;
    private final String  area;
    private final String  vendor;
    private final String  model;
    private final String  linkTo;
    private final String  country;
    private final Integer frequency;

    public Nodes(Nodes value) {
        this.id = value.id;
        this.name = value.name;
        this.master = value.master;
        this.area = value.area;
        this.vendor = value.vendor;
        this.model = value.model;
        this.linkTo = value.linkTo;
        this.country = value.country;
        this.frequency = value.frequency;
    }

    public Nodes(
        Integer id,
        String  name,
        String  master,
        String  area,
        String  vendor,
        String  model,
        String  linkTo,
        String  country,
        Integer frequency
    ) {
        this.id = id;
        this.name = name;
        this.master = master;
        this.area = area;
        this.vendor = vendor;
        this.model = model;
        this.linkTo = linkTo;
        this.country = country;
        this.frequency = frequency;
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getMaster() {
        return this.master;
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

    public String getLinkTo() {
        return this.linkTo;
    }

    public String getCountry() {
        return this.country;
    }

    public Integer getFrequency() {
        return this.frequency;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Nodes (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(master);
        sb.append(", ").append(area);
        sb.append(", ").append(vendor);
        sb.append(", ").append(model);
        sb.append(", ").append(linkTo);
        sb.append(", ").append(country);
        sb.append(", ").append(frequency);

        sb.append(")");
        return sb.toString();
    }
}