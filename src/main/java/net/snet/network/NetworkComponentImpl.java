package net.snet.network;

import org.jooq.DSLContext;

public class NetworkComponentImpl implements NetworkComponent {
  private final NetworkRepository networkRepository;

  public NetworkComponentImpl(DSLContext dslContext) {
    this.networkRepository = new JooqNetworkRepository(dslContext);
  }

  @Override
  public Iterable<Node> findNodes(NodeQuery query) {
    return networkRepository.findNodes(query);
  }
}
