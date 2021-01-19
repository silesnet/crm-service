package net.snet.network.command.domain.model;

import net.snet.network.NodeId;

public interface NetworkWriteRepository {
  Node insertNode(Node node);
  Node updateNode(Node node);
  void deleteNode(NodeId nodeId);
}
