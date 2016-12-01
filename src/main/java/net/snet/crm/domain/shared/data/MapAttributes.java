package net.snet.crm.domain.shared.data;

import java.util.Map;

public class MapAttributes implements Attributes {

  public static Attributes of(Map<String, Object> map) {
    return new MapAttributes(map);
  }

  private final Data data;

  private MapAttributes(Map<String, Object> map) {
    this.data = MapData.of(map);
  }

  @Override
  public Data attributesData() {
    return data;
  }
}
