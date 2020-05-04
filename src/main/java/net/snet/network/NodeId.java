package net.snet.network;

import lombok.Value;

import java.util.regex.Pattern;

@Value
public class NodeId {
  private static final Pattern NODE_NAME = Pattern.compile("[a-z][a-z0-9-]+");
  private static final Pattern NODE_ID_NUMBER = Pattern.compile("[1-9]\\d*");

  private final String value;

  public NodeId(String value) {
    this.value = value;
    if (!isName() && !isIdNumber()) {
      throw new IllegalArgumentException("invalid network node id: '" + value + "'");
    }
  }

  public boolean isName() {
    return NODE_NAME.matcher(value).matches();
  }

  public boolean isIdNumber() {
    return NODE_ID_NUMBER.matcher(value).matches();
  }
}
