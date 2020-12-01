package net.snet.network.command.domain.model;

import lombok.Value;

import java.util.Map;

@Value
public class Node {
  Integer id;
  Map<String, Object> attributes;
}
