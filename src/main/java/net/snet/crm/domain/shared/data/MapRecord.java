package net.snet.crm.domain.shared.data;

import java.util.Map;

public class MapRecord implements Record {

  public static Record of(Map<String, Object> map) {
    return new MapRecord(map);
  }

  private final Data data;

  private MapRecord(Map<String, Object> map) {
    this.data = MapData.of(map);
  }

  @Override
  public Data recordData() {
    return data;
  }
}
