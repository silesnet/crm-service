/*
 * This file is generated by jOOQ.
 */
package net.snet.crm.infra.db.query;


import javax.annotation.Generated;

import net.snet.crm.infra.db.query.tables.Nodes;
import net.snet.crm.infra.db.query.tables.NodesDetail;


/**
 * Convenience access to all tables in query
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.12.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>query.nodes</code>.
     */
    public static final Nodes NODES = Nodes.NODES;

    /**
     * The table <code>query.nodes_detail</code>.
     */
    public static final NodesDetail NODES_DETAIL = NodesDetail.NODES_DETAIL;
}
