package net.snet.network.command;

import lombok.extern.slf4j.Slf4j;
import net.snet.crm.infra.db.command.Tables;
import net.snet.crm.infra.db.command.tables.records.NetworkRecord;
import net.snet.network.NodeId;
import net.snet.network.command.domain.model.NetworkWriteRepository;
import net.snet.network.command.domain.model.Node;
import org.jooq.DSLContext;

import java.util.List;

import static net.snet.crm.infra.db.command.tables.Network.NETWORK;
import static net.snet.crm.infra.db.query.Tables.NODES;

@Slf4j
public class JooqNetworkWriteRepository implements NetworkWriteRepository {
  private final DSLContext db;

  public JooqNetworkWriteRepository(DSLContext db) {
    this.db = db;
  }

  @Override
  public Node insertNode(Node node) {
    LOGGER.info("inserting node {}", node.toString());
    final NetworkRecord networkRecord = db.newRecord(Tables.NETWORK);
    networkRecord.fromMap(node.getAttributes());
    LOGGER.info("network record before insert {}", networkRecord);
    networkRecord.insert();
    LOGGER.info("network record before insert {}", networkRecord);
    final Node inserted = new Node(networkRecord.getId(), networkRecord.intoMap());
    LOGGER.info("inserted node {}", inserted.toString());
    return inserted;
  }

  @Override
  public Node updateNode(Node node) {
    LOGGER.info("updating node {}", node.toString());
    final NetworkRecord record = db.fetchOne(NETWORK, NETWORK.ID.eq(node.getId()));
    record.fromMap(node.getAttributes());
    LOGGER.info("network record before update {}", record);
    record.store();
    LOGGER.info("network record after update {}", record);
    final Node updated = new Node(record.getId(), record.intoMap());
    LOGGER.info("updated node {}", updated.toString());
    return updated;
  }

  @Override
  public void deleteNode(NodeId nodeId) {
    LOGGER.info("deleting node {}", nodeId.getValue());
    final List<String> children = db.select(NODES.NAME).from(NODES)
        .where(NODES.LINK_TO.eq(db.select(NODES.NAME).from(NODES).where(NODES.ID.eq(nodeId.getNumberValue()))))
        .orderBy(NODES.NAME)
        .fetchInto(String.class);
    if (children.size() > 0) {
      throw new IllegalStateException("can not delete node '" + nodeId.getValue() + "' as there are nodes that link to it: " + children);
    }
    db.deleteFrom(NETWORK).where(NETWORK.ID.eq(nodeId.getNumberValue())).execute();
  }
}
