package net.snet.network;

import lombok.Value;

@Value
public class Node {
  private final Integer id;
  private final String  name;
  private final String  master;
  private final String  area;
  private final String  vendor;
  private final String  model;
  private final String  linkTo;
  private final Integer rstpNumRing;
  private final String  backupPath;
}
