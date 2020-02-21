package net.snet.network;

import com.google.common.collect.Iterables;
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

  @Override
  public Iterable<Node> findNodes(NodeFilter filter) {
    return Iterables.cycle();
  }
}
