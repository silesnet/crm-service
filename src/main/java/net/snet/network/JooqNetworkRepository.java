package net.snet.network;

import lombok.extern.slf4j.Slf4j;
import net.snet.crm.infra.db.jooq.tables.pojos.NetworkNodesView;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.stream.Collectors;

import static net.snet.crm.infra.db.jooq.tables.NetworkNodesView.NETWORK_NODES_VIEW;

@Slf4j
class JooqNetworkRepository implements NetworkRepository {
  private final DSLContext db;

  public JooqNetworkRepository(DSLContext dslContext) {
    this.db = dslContext;
  }

  @Override
  public Iterable<Node> findNodes(NodeQuery query) {
    LOGGER.debug("find nodes by '{}'", query);
    return db.select().from(NETWORK_NODES_VIEW)
        .where(DSL.condition("to_tsvector('english', {0}) @@ to_tsquery('english', {1})",
            DSL.concat(NETWORK_NODES_VIEW.NAME,
                DSL.val(" "), DSL.coalesce(NETWORK_NODES_VIEW.MASTER, ""),
                DSL.val(" "), DSL.coalesce(NETWORK_NODES_VIEW.VENDOR, ""),
                DSL.val(" "), DSL.coalesce(NETWORK_NODES_VIEW.LINK_TO, ""),
                DSL.val(" "), DSL.coalesce(NETWORK_NODES_VIEW.AREA, "")),
            DSL.val(query.getValue() + ":*")))
        .limit(100)
        .fetchInto(NetworkNodesView.class)
        .stream()
        .map(node -> new Node(
            node.getId(),
            node.getName(),
            node.getMaster(),
            node.getArea(),
            node.getVendor(),
            node.getModel(),
            node.getLinkTo(),
            node.getRstpNumRing(),
            node.getBackupPath()
        ))
        .collect(Collectors.toList());
  }

}
