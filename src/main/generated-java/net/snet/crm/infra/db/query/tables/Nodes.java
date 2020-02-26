/*
 * This file is generated by jOOQ.
 */
package net.snet.crm.infra.db.query.tables;


import javax.annotation.Generated;

import net.snet.crm.infra.db.query.Query;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
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
public class Nodes extends TableImpl<Record> {

    private static final long serialVersionUID = -570662120;

    /**
     * The reference instance of <code>query.nodes</code>
     */
    public static final Nodes NODES = new Nodes();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<Record> getRecordType() {
        return Record.class;
    }

    /**
     * The column <code>query.nodes.id</code>.
     */
    public final TableField<Record, Integer> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>query.nodes.name</code>.
     */
    public final TableField<Record, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>query.nodes.master</code>.
     */
    public final TableField<Record, String> MASTER = createField(DSL.name("master"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>query.nodes.area</code>.
     */
    public final TableField<Record, String> AREA = createField(DSL.name("area"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>query.nodes.vendor</code>.
     */
    public final TableField<Record, String> VENDOR = createField(DSL.name("vendor"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>query.nodes.model</code>.
     */
    public final TableField<Record, String> MODEL = createField(DSL.name("model"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>query.nodes.link_to</code>.
     */
    public final TableField<Record, String> LINK_TO = createField(DSL.name("link_to"), org.jooq.impl.SQLDataType.VARCHAR(50), this, "");

    /**
     * The column <code>query.nodes.country</code>.
     */
    public final TableField<Record, String> COUNTRY = createField(DSL.name("country"), org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>query.nodes.frequency</code>.
     */
    public final TableField<Record, Integer> FREQUENCY = createField(DSL.name("frequency"), org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * Create a <code>query.nodes</code> table reference
     */
    public Nodes() {
        this(DSL.name("nodes"), null);
    }

    /**
     * Create an aliased <code>query.nodes</code> table reference
     */
    public Nodes(String alias) {
        this(DSL.name(alias), NODES);
    }

    /**
     * Create an aliased <code>query.nodes</code> table reference
     */
    public Nodes(Name alias) {
        this(alias, NODES);
    }

    private Nodes(Name alias, Table<Record> aliased) {
        this(alias, aliased, null);
    }

    private Nodes(Name alias, Table<Record> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Nodes(Table<O> child, ForeignKey<O, Record> key) {
        super(child, key, NODES);
    }

    @Override
    public Schema getSchema() {
        return Query.QUERY;
    }

    @Override
    public Nodes as(String alias) {
        return new Nodes(DSL.name(alias), this);
    }

    @Override
    public Nodes as(Name alias) {
        return new Nodes(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Nodes rename(String name) {
        return new Nodes(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Nodes rename(Name name) {
        return new Nodes(name, null);
    }
}