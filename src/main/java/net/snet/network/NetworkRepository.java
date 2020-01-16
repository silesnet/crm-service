package net.snet.network;

interface NetworkRepository {
  Iterable<Node> findNodes(NodeQuery query);
}
