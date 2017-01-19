package net.snet.crm.domain.shared.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MapData implements Data
{

  private static final Pattern NUMBER = Pattern.compile("\\d+");

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
  public boolean hasBoolean(String path) {
    return isBoolean(valueOf(path));
  }

  @Override
  public boolean hasNumber(String path) {
    return isNumeric(valueOf(path));
  }

  @Override
  public boolean hasDate(String path) {
    return isDate(valueOf(path));
  }

  @Override
  public boolean hasDateTime(String path) {
    return isDateTime(valueOf(path));
  }

  @Override
  public boolean hasMap(String path) {
    return isMap(valueOf(path));
  }

  @Override
  public boolean hasData(String path) {
    return isMap(valueOf(path));
  }

  @Override
  public boolean hasList(String path) {
    return isList(valueOf(path));
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean booleanOf(String path) {
    return asBoolean(assertValueExistsOn(path));
  }

  @Override
  public boolean optBooleanOf(String path, boolean def) {
    final Value value = valueOf(path);
    return value.hasValue ? asBoolean(value) : def;
  }

  @Override
  public boolean optBooleanOf(String path) {
    return optBooleanOf(path, false);
  }

  @Override
  public int intOf(String path) {
    return asInt(assertValueExistsOn(path));
  }

  @Override
  public int optIntOf(String path, int def) {
    final Value value = valueOf(path);
    return isNumeric(value) ? asInt(value) : def;
  }

  @Override
  public int optIntOf(String path) {
    return optIntOf(path, 0);
  }

  @Override
  public long longOf(final String path) {
    return asLong(assertValueExistsOn(path));
  }

  @Override
  public long optLongOf(String path) {
    return optLongOf(path, 0);
  }

  @Override
  public long optLongOf(String path, long def) {
    final Value value = valueOf(path);
    return isNumeric(value) ? asLong(value) : def;
  }

  @Override
  public String stringOf(String path) {
    return asString(assertValueExistsOn(path));
  }

  @Override
  public String optStringOf(String path, String def) {
    final Value value = valueOf(path);
    return value.hasValue ? asString(value) : def;
  }

  @Override
  public String optStringOf(String path) {
    return optStringOf(path, "");
  }

  @Override
  public LocalDate dateOf(String path) {
    return asDate(assertValueExistsOn(path));
  }

  @Override
  public LocalDate optDateOf(String path, LocalDate def) {
    final Value value = valueOf(path);
    return isDate(value) ? asDate(value) : def;
  }

  @Override
  public LocalDate optDateOf(String path) {
    return optDateOf(path, LocalDate.now());
  }

  @Override
  public DateTime dateTimeOf(String path) {
    return asDateTime(assertValueExistsOn(path));
  }

  @Override
  public DateTime optDateTimeOf(String path, DateTime def) {
    final Value value = valueOf(path);
    return isDateTime(value) ? asDateTime(value) : def;
  }

  @Override
  public DateTime optDateTimeOf(String path) {
    return optDateTimeOf(path, DateTime.now());
  }

  @Override
  public Map<String, Object> mapOf(String path) {
    final Value value = assertValueExistsOn(path);
    return value.isLeaf ? ImmutableMap.<String, Object>of() : Maps.newHashMap(asMap(value));
  }

  @Override
  public Map<String, Object> optMapOf(String path) {
    final Value value = valueOf(path);
    return value.hasValue ? asMap(value) : ImmutableMap.<String, Object>of();
  }

  @Override
  public List<Object> listOf(String path) {
    final Value value = assertValueExistsOn(path);
    return asList(value);
  }

  @Override
  public List<Object> optListOf(String path) {
    final Value value = valueOf(path);
    return value.hasValue ? asList(value) : ImmutableList.of();
  }

  @Override
  public Data dataOf(String path) {
    final Value value = assertValueExistsOn(path);
    return asData(value);
  }

  @Override
  public Data optDataOf(String path) {
    final Value value = valueOf(path);
    return value.hasValue ? asData(value) : Data.EMPTY;
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.newHashMap(map);
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

  private LocalDate asDate(Value value) {
    return (value.value instanceof LocalDate)
        ? (LocalDate) value.value
        : LocalDate.parse(value.value.toString());
  }

  private DateTime asDateTime(Value value) {
    return (value.value instanceof DateTime)
        ? (DateTime) value.value
        : DateTime.parse(value.value.toString().replace(' ', 'T'));
  }

  private Map<String, Object> asMap(Value value) {
    if (!(value.value instanceof Map)) {
      throw new IllegalStateException("value is not a map");
    }
    return (Map<String, Object>) value.value;
  }

  private List<Object> asList(Value value) {
    if (!(value.value instanceof List)) {
      throw new IllegalStateException("value is not a list");
    }
    return (List<Object>) value.value;
  }

  private Data asData(Value value) {
    return MapData.of(asMap(value));
  }

  private Value assertValueExistsOn(final String path) {
    final Value value = valueOf(path);
    if (!value.exists) {
      throw new IllegalArgumentException("value doesn't exist on '" + value.path + "' path");
    }
    return value;
  }

  private boolean isBoolean(Value value) {
    if (!value.hasValue) {
      return false;
    }
    return "true".equalsIgnoreCase(value.value.toString());
  }

  private boolean isNumeric(Value value) {
    if (!value.hasValue) {
      return false;
    }
    return NUMBER.matcher(value.value.toString()).matches();
  }

  private boolean isDateTime(Value value) {
    if (!value.hasValue) {
      return false;
    }
    try {
      DateTime.parse(value.value.toString());
      return true;
    } catch (Exception e) {
      // ignore
    }
    return false;
  }

  private boolean isDate(Value value) {
    if (!value.hasValue) {
      return false;
    }
    try {
      LocalDate.parse(value.value.toString());
      return true;
    } catch (Exception e) {
      // ignore
    }
    return false;
  }

  private boolean isMap(Value value) {
    if (!value.hasValue) {
      return false;
    }
    return value.value instanceof Map;
  }

  private boolean isList(Value value) {
    if (!value.hasValue) {
      return false;
    }
    return value.value instanceof List;
  }


  private Value valueOf(String path) {
    return new Value(path);
  }

  private class Value
  {
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

  @Override
  public String toString() {
    return map.toString();
  }
}
