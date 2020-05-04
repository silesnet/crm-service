package net.snet.network;

import lombok.Value;

@Value
public class NodeItem {
  private final Integer id;
  private final String name;
  private final String master;
  private final String area;
  private final String vendor;
  private final String model;
  private final String linkTo;
  private final String country;
  private final Integer frequency;
}
