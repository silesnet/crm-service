package net.snet.network;

import lombok.extern.slf4j.Slf4j;
import net.snet.crm.infra.db.jooq.tables.pojos.NetworkNodesView;
import org.jooq.DSLContext;

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
        .limit(10)
        .fetchInto(NetworkNodesView.class)
        .stream()
        .map(node -> new Node(
            node.getName(),
            node.getMaster(),
            node.getArea()
        ))
        .collect(Collectors.toList());
  }
}
