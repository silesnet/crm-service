package net.snet.network;

import org.jooq.DSLContext;

import java.util.Optional;

public class NetworkComponentImpl implements NetworkComponent {
  private final NetworkRepository networkRepository;

  public NetworkComponentImpl(DSLContext dslContext) {
    this.networkRepository = new JooqNetworkRepository(dslContext);
  }

  @Override
  public Iterable<NodeItem> findNodes(NodeQuery query) {
    return networkRepository.findNodes(query);
  }

  @Override
  public Iterable<NodeItem> findNodes(NodeFilter filter) {
    return networkRepository.findNodes(filter);
  }

  @Override
  public Optional<Node> fetchNode(NodeId nodeId) {
    return networkRepository.fetchNode(nodeId);
  }
}
