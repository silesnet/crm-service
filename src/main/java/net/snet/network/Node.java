package net.snet.network;

import lombok.Value;

@Value
public class Node {
  private final String name;
  private final String master;
  private final String area;
}
