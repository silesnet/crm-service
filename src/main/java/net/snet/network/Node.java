package net.snet.network;

import lombok.Value;

@Value
public class Node {
   int id;
   String country;
   String name;
   String type;
   String master;
   String linkTo;
   String area;
   String vendor;
   String model;
   String info;
   String monitoring;
   String path;
   String ping;
   boolean isWireless;
   String polarization;
   String width;
   String norm;
   Boolean tdma;
   Boolean aggregation;
   String ssid;
   Integer frequency;
   String power;
   String antenna;
   Boolean wds;
   String authentication;
   String azimuth;
   Boolean active;
}
