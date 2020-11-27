package net.snet.network.command.domain.model;

import lombok.Value;

import java.util.Map;

@Value
public class Node {
  Map<String, Object> attributes;
}
