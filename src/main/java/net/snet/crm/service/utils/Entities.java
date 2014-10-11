package net.snet.crm.service.utils;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;

import java.util.Map;

@SuppressWarnings("unchecked")
public class Entities {

  public static Optional<Object> fetchNested(String path,
                                             Map<String, Object> map) {
    return Optional.fromNullable(fetchNestedInternal(path, map));
  }

  public static Optional<Map<String, Object>> fetchNestedMap(String path,
                                                   Map<String, Object> map) {
    final Object nested = fetchNestedInternal(path, map);
    return Optional.fromNullable((Map<String, Object>) nested);
  }

  private static Object fetchNestedInternal(String path,
                                                     Map<String, Object> map) {
    Object value = map;
    for (String key : Splitter.on('.').split(path)) {
      value = ((Map<String, Object>) value).get(key);
      if (value == null) {
        break;
      }
    }
    return value;
  }

}
