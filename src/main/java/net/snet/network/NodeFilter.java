package net.snet.network;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NodeFilter {
  private final String name;
  private final String master;
  private final String area;
  private final String linkTo;
  private final String vendor;
  private final String country;
}
