package net.snet.crm.service.utils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;

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
      } else if (record.containsKey(property)) {
        entity.put(property, record.get(property));
      }
    }
    return entity;
  }

  public static Optional<Object> optionalOf(
      String path,
      Map<String, Object> map) {
    return optionalOf(path, map, Object.class);
  }

//  @Nonnull
//  public static Object valueOf(
//      @Nonnull String path,
//      @Nonnull Map<String, Object> map) {
//    return valueOf(path, map, Object.class);
//  }

  @SuppressWarnings("unchecked unused")
  public static <T> Optional<T> optionalOf(String path,
                                           Map<String, ?> map,
                                           Class<T> klazz) {
    return Optional.fromNullable((T) fetchNestedInternal(path, map));
  }

  @SuppressWarnings("unused")
  public static <T> T valueOf(String path,
                              Map<String, ?> map,
                              Class<T> klazz) {
    Object value = fetchNestedInternal(path, map);
    if (value == null) {
      throw new WebApplicationException(
          new IllegalArgumentException(
              "value get '" + path + "' does not exist or is null"),
          Response.Status.BAD_REQUEST
      );
    } else {
      if (Long.class.equals(klazz) && value instanceof Integer) {
        value = ((Integer) value).longValue();
      }
    }
    return (T) value;
  }

  public static <T> T cast(Object obj, Class<T> klazz) {
    T value = null;
    if (obj != null) {
      if (Integer.class.equals(klazz)) {
        value = (T) Integer.valueOf("" + obj);
      } else {
        value = (T) obj;
      }
    }
    return value;
  }


  public static Optional<Map<String, Object>> optionalMapOf(
      String path,
      Map<String, Object> map) {
    final Object nested = fetchNestedInternal(path, map);
    return Optional.fromNullable((Map<String, Object>) nested);
  }


  public static Map<String, Object> mapOf(String path,
                                          Map<String, Object> map) {
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


  public static Function<Map<String, Object>, String> getValueOf(final String key) {
    return new Function<Map<String, Object>, String>() {
      @Nullable
      @Override
      public String apply(Map<String, Object> userData) {
        return String.valueOf(userData.get(key));
      }
    };
  }

  public static Value valueOf(final String path,
                              final Map<String, ?> map) {
    return new Value(fetchNestedInternal(path, map));
  }

  public static ValueMap valueMapOf(final Map<String, ?> map) {
    return new ValueMap(map);
  }

  public static class Value {
    private final Object original;

    public Value(@Nullable Object value) {
      this.original = value;
    }


    public String asString() {
      return original.toString();
    }

    @Nullable
    public String asStringOr(@Nullable final String fallBack) {
      return isNull() ? fallBack : asString();
    }

    public String asStringValueOr(@Nullable final String fallback) {
      return isNullOrEmpty() ? fallback : asString();
    }


    public long asLong() {
      return Long.valueOf(original.toString());
    }


    public Long asLongOr(final Long fallBack) {
      return isNullOrEmpty() ? fallBack : (Long) asLong();
    }


    public Integer asInteger() {
      return Integer.valueOf(original.toString());
    }

    public Integer asIntegerOr(final Integer fallBack) {
      return isNullOrEmpty() ? fallBack : asInteger();
    }

    public DateTime asDateTime() {
      return DateTime.parse(original.toString());
    }

    public DateTime asDateTimeOr(@Nullable final DateTime fallBack) {
      return isNullOrEmpty() ? fallBack : asDateTime();
    }

    public ValueMap asMap() {
      return valueMapOf((Map<String, ?>) original);
    }

    public boolean isNull() {
      return original == null;
    }

    private boolean isNullOrEmpty() {
      return original == null || original.toString().trim().length() == 0;
    }

    @Override
    public String toString() {
      return isNull() ? "" : original.toString();
    }
  }

  public static class ValueMap {
    private final Map<String, ?> map;

    private ValueMap(final Map<String, ?> map) {
      this.map = map;
    }

    public Value get(final String path) {
      return valueOf(path, map);
    }

    public Object getRaw(final String path) {
      return fetchNestedInternal(path, map);
    }

    public Object getRawOr(final String path, @Nullable final Object fallBack) {
      final Object value = fetchNestedInternal(path, map);
      return value == null ? fallBack : value;
    }

    public Map<String, Object> map() {
      return (Map<String, Object>) map;
    }
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
