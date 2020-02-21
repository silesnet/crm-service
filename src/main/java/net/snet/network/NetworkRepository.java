package net.snet.network;

interface NetworkRepository {
  Iterable<Node> findNodes(NodeQuery query);
  Iterable<Node> findNodes(NodeFilter filter);
}
