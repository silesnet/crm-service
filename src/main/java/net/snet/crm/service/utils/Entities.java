package net.snet.crm.service.utils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
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

  @Nonnull
  public static Optional<Object> optionalOf(
      @Nonnull String path,
      @Nonnull Map<String, Object> map) {
    return optionalOf(path, map, Object.class);
  }

  @Nonnull
  public static Object valueOf(
      @Nonnull String path,
      @Nonnull Map<String, Object> map) {
    return valueOf(path, map, Object.class);
  }

  @Nonnull
  @SuppressWarnings("unused")
  public static <T> Optional<T> optionalOf(@Nonnull String path,
                                           @Nonnull Map<String, ?> map,
                                           @Nonnull Class<T> klazz) {
    return Optional.fromNullable((T) fetchNestedInternal(path, map));
  }

  @Nonnull
  @SuppressWarnings("unused")
  public static <T> T valueOf(@Nonnull String path,
                              @Nonnull Map<String, ?> map,
                              @Nonnull Class<T> klazz) {
    final Object value = fetchNestedInternal(path, map);
    if (value == null) {
      throw new WebApplicationException(
          new IllegalArgumentException(
              "value of '" + path + "' does not exist or is null"),
          Response.Status.BAD_REQUEST
      );
    }
    return (T) value;
  }

  @Nonnull
  public static Optional<Map<String, Object>> optionalMapOf(
      @Nonnull String path,
      @Nonnull Map<String, Object> map) {
    final Object nested = fetchNestedInternal(path, map);
    return Optional.fromNullable((Map<String, Object>) nested);
  }

  @Nonnull
  public static Map<String, Object> mapOf(@Nonnull String path,
                                          @Nonnull Map<String, Object> map) {
    final Object nested = fetchNestedInternal(path, map);
    if (nested == null) {
      throw new WebApplicationException(
          new IllegalArgumentException(
              "map at '" + path + "' does not exist"),
          Response.Status.BAD_REQUEST
      );
    }
    return (Map<String, Object>) nested;
  }

  @Nonnull
  public static Function<Map<String, Object>, String> getValueOf(final String key) {
    return new Function<Map<String,Object>, String>() {
      @Nullable
      @Override
      public String apply(Map<String, Object> userData) {
        return String.valueOf(userData.get(key));
      }
    };
  }

  private static Object fetchNestedInternal(@Nonnull String path,
                                            @Nonnull Map<String, ?> map) {
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
