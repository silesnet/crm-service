package net.snet.crm.domain.shared.data;

import java.util.Map;

public class MapProps implements Props {

  public static Props of(Map<String, Object> map) {
    return new MapProps(map);
  }

  private final Data data;

  private MapProps(Map<String, Object> map) {
    this.data = MapData.of(map);
  }

  @Override
  public Data propsData() {
    return data;
  }
}
