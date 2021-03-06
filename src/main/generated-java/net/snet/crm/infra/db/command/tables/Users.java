/*
 * This file is generated by jOOQ.
 */
package net.snet.crm.infra.db.command.tables;


import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import net.snet.crm.infra.db.command.Indexes;
import net.snet.crm.infra.db.command.Keys;
import net.snet.crm.infra.db.command.Public;
import net.snet.crm.infra.db.command.tables.records.UsersRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row9;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
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
public class Users extends TableImpl<UsersRecord> {

    private static final long serialVersionUID = 2005894919;

    /**
     * The reference instance of <code>public.users</code>
     */
    public static final Users USERS = new Users();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<UsersRecord> getRecordType() {
        return UsersRecord.class;
    }

    /**
     * The column <code>public.users.id</code>.
     */
    public final TableField<UsersRecord, Long> ID = createField(DSL.name("id"), org.jooq.impl.SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.users.login</code>.
     */
    public final TableField<UsersRecord, String> LOGIN = createField(DSL.name("login"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.users.passwd</code>.
     */
    public final TableField<UsersRecord, String> PASSWD = createField(DSL.name("passwd"), org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.users.name</code>.
     */
    public final TableField<UsersRecord, String> NAME = createField(DSL.name("name"), org.jooq.impl.SQLDataType.VARCHAR(255).nullable(false), this, "");

    /**
     * The column <code>public.users.roles</code>.
     */
    public final TableField<UsersRecord, String> ROLES = createField(DSL.name("roles"), org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.users.key</code>.
     */
    public final TableField<UsersRecord, String> KEY = createField(DSL.name("key"), org.jooq.impl.SQLDataType.VARCHAR(255), this, "");

    /**
     * The column <code>public.users.reports_to</code>.
     */
    public final TableField<UsersRecord, Long> REPORTS_TO = createField(DSL.name("reports_to"), org.jooq.impl.SQLDataType.BIGINT.nullable(false).defaultValue(org.jooq.impl.DSL.field("0", org.jooq.impl.SQLDataType.BIGINT)), this, "");

    /**
     * The column <code>public.users.operation_country</code>.
     */
    public final TableField<UsersRecord, String> OPERATION_COUNTRY = createField(DSL.name("operation_country"), org.jooq.impl.SQLDataType.CHAR(2).nullable(false).defaultValue(org.jooq.impl.DSL.field("'CZ'::bpchar", org.jooq.impl.SQLDataType.CHAR)), this, "");

    /**
     * The column <code>public.users.full_name</code>.
     */
    public final TableField<UsersRecord, String> FULL_NAME = createField(DSL.name("full_name"), org.jooq.impl.SQLDataType.CLOB.nullable(false).defaultValue(org.jooq.impl.DSL.field("''::text", org.jooq.impl.SQLDataType.CLOB)), this, "");

    /**
     * Create a <code>public.users</code> table reference
     */
    public Users() {
        this(DSL.name("users"), null);
    }

    /**
     * Create an aliased <code>public.users</code> table reference
     */
    public Users(String alias) {
        this(DSL.name(alias), USERS);
    }

    /**
     * Create an aliased <code>public.users</code> table reference
     */
    public Users(Name alias) {
        this(alias, USERS);
    }

    private Users(Name alias, Table<UsersRecord> aliased) {
        this(alias, aliased, null);
    }

    private Users(Name alias, Table<UsersRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Users(Table<O> child, ForeignKey<O, UsersRecord> key) {
        super(child, key, USERS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.SIS_USER_LOGIN_KEY, Indexes.SIS_USER_NAME_KEY, Indexes.SIS_USER_PKEY);
    }

    @Override
    public UniqueKey<UsersRecord> getPrimaryKey() {
        return Keys.SIS_USER_PKEY;
    }

    @Override
    public List<UniqueKey<UsersRecord>> getKeys() {
        return Arrays.<UniqueKey<UsersRecord>>asList(Keys.SIS_USER_PKEY, Keys.SIS_USER_LOGIN_KEY, Keys.SIS_USER_NAME_KEY);
    }

    @Override
    public Users as(String alias) {
        return new Users(DSL.name(alias), this);
    }

    @Override
    public Users as(Name alias) {
        return new Users(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Users rename(String name) {
        return new Users(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Users rename(Name name) {
        return new Users(name, null);
    }

    // -------------------------------------------------------------------------
    // Row9 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row9<Long, String, String, String, String, String, Long, String, String> fieldsRow() {
        return (Row9) super.fieldsRow();
    }
}
