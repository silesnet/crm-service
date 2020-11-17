package net.snet.network;

import lombok.Value;

@Value
public class Node {
  private final int id;
  private final String country;
  private final String name;
  private final String type;
  private final String master;
  private final String linkTo;
  private final String area;
  private final String vendor;
  private final String model;
  private final String info;
  private final String monitoring;
  private final String path;
  private final String ping;
  private final boolean isWireless;
  private final String polarization;
  private final String width;
  private final String norm;
  private final Boolean tdma;
  private final Boolean aggregation;
  private final String ssid;
  private final Integer frequency;
  private final String power;
  private final String antenna;
  private final Boolean wds;
  private final String authentication;
  private final String azimuth;
  private final Boolean active;
}
