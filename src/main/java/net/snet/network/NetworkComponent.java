package net.snet.network;

import java.util.Map;
import java.util.Optional;

public interface NetworkComponent {
  Iterable<NodeItem> findNodes(NodeQuery query);
  Iterable<NodeItem> findNodes(NodeFilter filter);
  Optional<Node> fetchNode(NodeId nodeId);
  Map<String, Iterable<String>> fetchNodeOptions();
}
