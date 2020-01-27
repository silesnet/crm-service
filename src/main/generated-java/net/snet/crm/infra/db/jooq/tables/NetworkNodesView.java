/*
 * This file is generated by jOOQ.
 */
package net.snet.crm.infra.db.jooq.tables;


import javax.annotation.Generated;

import net.snet.crm.infra.db.jooq.Public;
import net.snet.crm.infra.db.jooq.tables.records.NetworkNodesViewRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row9;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


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
public class NetworkNodesView extends TableImpl<NetworkNodesViewRecord> {

    private static final long serialVersionUID = -1597109496;

    /**
     * The reference instance of <code>public.network_nodes_view</code>
     */
    public static final NetworkNodesView NETWORK_NODES_VIEW = new NetworkNodesView();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<NetworkNodesViewRecord> getRecordType() {
        return NetworkNodesViewRecord.class;
    }

    /**
     * The column <code>public.network_nodes_view.id</code>.
     */
    public final TableField<NetworkNodesViewRecord, Integer> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.network_nodes_view.name</code>.
     */
    public final TableField<NetworkNodesViewRecord, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>public.network_nodes_view.master</code>.
     */
    public final TableField<NetworkNodesViewRecord, String> MASTER = createField(DSL.name("master"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>public.network_nodes_view.area</code>.
     */
    public final TableField<NetworkNodesViewRecord, String> AREA = createField(DSL.name("area"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>public.network_nodes_view.vendor</code>.
     */
    public final TableField<NetworkNodesViewRecord, String> VENDOR = createField(DSL.name("vendor"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>public.network_nodes_view.model</code>.
     */
    public final TableField<NetworkNodesViewRecord, String> MODEL = createField(DSL.name("model"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>public.network_nodes_view.link_to</code>.
     */
    public final TableField<NetworkNodesViewRecord, String> LINK_TO = createField(DSL.name("link_to"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>public.network_nodes_view.rstp_num_ring</code>.
     */
    public final TableField<NetworkNodesViewRecord, Integer> RSTP_NUM_RING = createField(DSL.name("rstp_num_ring"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>public.network_nodes_view.backup_path</code>.
     */
    public final TableField<NetworkNodesViewRecord, String> BACKUP_PATH = createField(DSL.name("backup_path"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * Create a <code>public.network_nodes_view</code> table reference
     */
    public NetworkNodesView() {
        this(DSL.name("network_nodes_view"), null);
    }

    /**
     * Create an aliased <code>public.network_nodes_view</code> table reference
     */
    public NetworkNodesView(String alias) {
        this(DSL.name(alias), NETWORK_NODES_VIEW);
    }

    /**
     * Create an aliased <code>public.network_nodes_view</code> table reference
     */
    public NetworkNodesView(Name alias) {
        this(alias, NETWORK_NODES_VIEW);
    }

    private NetworkNodesView(Name alias, Table<NetworkNodesViewRecord> aliased) {
        this(alias, aliased, null);
    }

    private NetworkNodesView(Name alias, Table<NetworkNodesViewRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> NetworkNodesView(Table<O> child, ForeignKey<O, NetworkNodesViewRecord> key) {
        super(child, key, NETWORK_NODES_VIEW);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public NetworkNodesView as(String alias) {
        return new NetworkNodesView(DSL.name(alias), this);
    }

    @Override
    public NetworkNodesView as(Name alias) {
        return new NetworkNodesView(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public NetworkNodesView rename(String name) {
        return new NetworkNodesView(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public NetworkNodesView rename(Name name) {
        return new NetworkNodesView(name, null);
    }

    // -------------------------------------------------------------------------
    // Row9 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row9<Integer, String, String, String, String, String, String, Integer, String> fieldsRow() {
        return (Row9) super.fieldsRow();
    }
}
