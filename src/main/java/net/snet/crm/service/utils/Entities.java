package net.snet.crm.service.utils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Entities {

  public static Map<String, Object> recordOf(final Map<String, Object> entity,
                                             final Map<String, String> columnMap) {
    final Map<String, Object> record = Maps.newLinkedHashMap();
    for (Map.Entry<String, String> mapping : columnMap.entrySet()) {
      final String property = mapping.getKey();
      final String column = mapping.getValue();
      if (entity.containsKey(property)) {
        record.put(column, entity.get(property));
      } else if (entity.containsKey(column)) {
        record.put(column, entity.get(column));
      }
    }
    return record;
  }

  public static Map<String, Object> entityOf(final Map<String, Object> record,
                                             final Map<String, String> columnMap) {
    final Map<String, Object> entity = Maps.newLinkedHashMap();
    for (Map.Entry<String, String> mapping : columnMap.entrySet()) {
      final String property = mapping.getKey();
      final String column = mapping.getValue();
      if (record.containsKey(column)) {
        entity.put(property, record.get(column));
      }
    }
    return entity;
  }

  public static Optional<Object> fetchNested(String path,
                                             Map<String, Object> map) {
    return Optional.fromNullable(fetchNestedInternal(path, map));
  }

  public static Optional<Map<String, Object>> fetchNestedMap(String path,
                                                             Map<String, Object> map) {
    final Object nested = fetchNestedInternal(path, map);
    return Optional.fromNullable((Map<String, Object>) nested);
  }

  @SuppressWarnings("unused")
  public static <T> Optional<T> fetchNested(String path,
                                            Map<String, ?> map,
                                            Class<T> klazz) {
    return Optional.fromNullable((T) fetchNestedInternal(path, map));
  }

  public static Function<Map<String, Object>, String> getValueOf(final String key) {
    return new Function<Map<String,Object>, String>() {
      @Nullable
      @Override
      public String apply(Map<String, Object> userData) {
        return String.valueOf(userData.get(key));
      }
    };
  }

  private static Object fetchNestedInternal(String path,
                                            Map<String, ?> map) {
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
