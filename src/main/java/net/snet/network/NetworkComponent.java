package net.snet.network;

public interface NetworkComponent {
  Iterable<Node> findNodes(NodeQuery query);
  Iterable<Node> findNodes(NodeFilter filter);
}
