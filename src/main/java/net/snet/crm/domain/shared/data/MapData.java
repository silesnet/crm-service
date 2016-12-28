package net.snet.crm.domain.shared.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;

import java.util.Map;

public class MapData implements Data {
  public static Data of(Map<String, Object> map) {
    return new MapData(map);
  }

  private final Map<String, Object> map;

  private MapData(Map<String, Object> map) {
    if (map == null) {
      throw new IllegalArgumentException("null is not map");
    }
    this.map = map;
  }

  @Override
  public boolean hasPath(String path) {
    return valueOf(path).exists;
  }

  @Override
  public boolean hasValue(String path) {
    return valueOf(path).hasValue;
  }

  @Override
  public boolean booleanOf(String path) {
    return asBoolean(assertValueExistsOn(path));
  }

  @Override
  public boolean optionalBooleanOf(String path, boolean def) {
    final Value value = valueOf(path);
    return value.hasValue ? asBoolean(value) : def;
  }

  @Override
  public int intOf(String path) {
    return asInt(assertValueExistsOn(path));
  }

  @Override
  public int optionalIntOf(String path, int def) {
    final Value value = valueOf(path);
    return value.hasValue ? asInt(value) : def;
  }

  @Override
  public long longOf(final String path) {
    return asLong(assertValueExistsOn(path));
  }

  @Override
  public long optionalLongOf(String path, long def) {
    final Value value = valueOf(path);
    return value.hasValue ? asLong(value) : def;
  }

  @Override
  public String stringOf(String path) {
    return asString(assertValueExistsOn(path));
  }

  @Override
  public String optionalStringOf(String path, String def) {
    final Value value = valueOf(path);
    return value.hasValue ? asString(value) : def;
  }

  @Override
  public DateTime dateTimeOf(String path) {
    return asDateTime(assertValueExistsOn(path));
  }

  @Override
  public DateTime optionalDateTimeOf(String path, DateTime def) {
    final Value value = valueOf(path);
    return value.hasValue ? asDateTime(value) : def;
  }

  @Override
  public Map<String, Object> mapOf(String path) {
    final Value value = assertValueExistsOn(path);
    return value.isLeaf ? ImmutableMap.<String, Object>of() : Maps.newHashMap(asMap(value));
  }

  @Override
  public Map<String, Object> optionalMapOf(String path, Map<String, Object> def) {
    final Value value = valueOf(path);
    return value.hasValue ? asMap(value) : def;
  }

  @Override
  public Data dataOf(String path) {
    final Value value = assertValueExistsOn(path);
    return asData(value);
  }

  @Override
  public Data optionalDataOf(String path, Data def) {
    final Value value = valueOf(path);
    return value.hasValue ? asData(value) : def;
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.newHashMap(map);
  }

  private Data asData(Value value) {
    return MapData.of(asMap(value));
  }

  private boolean asBoolean(Value value) {
    return (value.value instanceof Boolean)
        ? (Boolean) value.value
        : Boolean.valueOf(value.value.toString());
  }

  private int asInt(Value value) {
    return (value.value instanceof Integer)
        ? (Integer) value.value
        : Integer.valueOf(value.value.toString());
  }

  private long asLong(Value value) {
    return (value.value instanceof Long)
        ? (Long) value.value
        : Long.valueOf(value.value.toString());
  }

  private String asString(Value value) {
    return (value.value instanceof String)
        ? (String) value.value
        : value.value.toString();
  }

  private DateTime asDateTime(Value value) {
    return (value.value instanceof DateTime)
        ? (DateTime) value.value
        : DateTime.parse(value.value.toString().replace(' ', 'T'));
  }

  private Map<String, Object> asMap(Value value) {
    if (!(value.value instanceof Map)) {
      throw new IllegalStateException("value is not map");
    }
    return (Map<String, Object>) value.value;
  }

  private Value assertValueExistsOn(final String path) {
    final Value value = valueOf(path);
    if (!value.exists) {
      throw new IllegalArgumentException("value doesn't exist on '" + value.path + "' path");
    }
    return value;
  }

  private Value valueOf(String path) {
    return new Value(path);
  }

  private class Value {
    private final boolean exists;
    private final boolean isNull;
    private final boolean hasValue;
    private final boolean isLeaf;
    private final String path;
    private final Object value;

    Value(String path) {
      if (path == null) {
        throw new IllegalArgumentException("value path can't be null");
      }
      this.path = path;
      final String[] parts = path.split("\\.");
      int depth = 0;
      boolean keyExists = false;
      Object tmp = map;
      for (String part : parts) {
        if (tmp instanceof Map) {
          Map<String, Object> subMap = (Map<String, Object>) tmp;
          depth++;
          keyExists = subMap.containsKey(part);
          tmp = subMap.get(part);
        } else {
          keyExists = false;
          tmp = null;
          break;
        }
      }
      this.exists = (depth == parts.length) && keyExists;
      this.value = tmp;
      this.isNull = tmp == null;
      this.hasValue = this.exists && !this.isNull;
      this.isLeaf = !(tmp instanceof Map);
    }
  }
}
