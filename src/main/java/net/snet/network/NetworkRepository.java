package net.snet.network;

import java.util.Optional;

interface NetworkRepository {
  Iterable<NodeItem> findNodes(NodeQuery query);
  Iterable<NodeItem> findNodes(NodeFilter filter);
  Optional<Node> fetchNode(NodeId nodeId);
}
