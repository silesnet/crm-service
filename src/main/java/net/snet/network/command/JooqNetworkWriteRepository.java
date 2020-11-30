package net.snet.network.command;

import lombok.extern.slf4j.Slf4j;
import net.snet.crm.infra.db.command.Tables;
import net.snet.crm.infra.db.command.tables.records.NetworkRecord;
import net.snet.network.command.domain.model.NetworkWriteRepository;
import net.snet.network.command.domain.model.Node;
import org.jooq.DSLContext;

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
    final Node inserted = new Node(networkRecord.intoMap());
    LOGGER.info("inserted node {}", inserted.toString());
    return inserted;
  }

  @Override
  public Node updateNode(Node node) {
    return null;
  }
}
