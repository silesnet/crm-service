/*
 * This file is generated by jOOQ.
 */
package net.snet.crm.infra.db.jooq.tables.pojos;


import java.io.Serializable;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;


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
@Entity
@Table(name = "network_nodes_view", schema = "public")
public class NetworkNodesView implements Serializable {

    private static final long serialVersionUID = 1000349593;

    private final Integer id;
    private final String  name;
    private final String  master;
    private final String  area;
    private final String  vendor;
    private final String  model;
    private final String  linkTo;
    private final Integer rstpNumRing;
    private final String  backupPath;

    public NetworkNodesView(NetworkNodesView value) {
        this.id = value.id;
        this.name = value.name;
        this.master = value.master;
        this.area = value.area;
        this.vendor = value.vendor;
        this.model = value.model;
        this.linkTo = value.linkTo;
        this.rstpNumRing = value.rstpNumRing;
        this.backupPath = value.backupPath;
    }

    public NetworkNodesView(
        Integer id,
        String  name,
        String  master,
        String  area,
        String  vendor,
        String  model,
        String  linkTo,
        Integer rstpNumRing,
        String  backupPath
    ) {
        this.id = id;
        this.name = name;
        this.master = master;
        this.area = area;
        this.vendor = vendor;
        this.model = model;
        this.linkTo = linkTo;
        this.rstpNumRing = rstpNumRing;
        this.backupPath = backupPath;
    }

    @Column(name = "id", precision = 32)
    public Integer getId() {
        return this.id;
    }

    @Column(name = "name", length = 50)
    @Size(max = 50)
    public String getName() {
        return this.name;
    }

    @Column(name = "master", length = 50)
    @Size(max = 50)
    public String getMaster() {
        return this.master;
    }

    @Column(name = "area", length = 50)
    @Size(max = 50)
    public String getArea() {
        return this.area;
    }

    @Column(name = "vendor", length = 50)
    @Size(max = 50)
    public String getVendor() {
        return this.vendor;
    }

    @Column(name = "model", length = 50)
    @Size(max = 50)
    public String getModel() {
        return this.model;
    }

    @Column(name = "link_to", length = 50)
    @Size(max = 50)
    public String getLinkTo() {
        return this.linkTo;
    }

    @Column(name = "rstp_num_ring", precision = 32)
    public Integer getRstpNumRing() {
        return this.rstpNumRing;
    }

    @Column(name = "backup_path")
    public String getBackupPath() {
        return this.backupPath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NetworkNodesView (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(master);
        sb.append(", ").append(area);
        sb.append(", ").append(vendor);
        sb.append(", ").append(model);
        sb.append(", ").append(linkTo);
        sb.append(", ").append(rstpNumRing);
        sb.append(", ").append(backupPath);

        sb.append(")");
        return sb.toString();
    }
}
