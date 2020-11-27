package net.snet.network.shared;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class NodeMapping {
  public static final Map<String, Integer> nodeTypes = new HashMap<>();
  static {
    nodeTypes.put("OTHER", 10);
    nodeTypes.put("ROUTER", 20);
    nodeTypes.put("BRIDGE", 30);
    nodeTypes.put("BRIDGE-AP", 30);
    nodeTypes.put("BRIDGE-BR", 30);
    nodeTypes.put("BRIDGE-STATION", 30);
    nodeTypes.put("SWITCH", 40);
    nodeTypes.put("CONVERTER", 50);
  }

  public static final Map<String, Integer> modeTypes = ImmutableMap.of(
      "BRIDGE-AP", 10,
      "BRIDGE-BR", 20,
      "BRIDGE-STATION", 30
  );

  public static final Map<String, String> authentications = ImmutableMap.of(
      "NONE", "10",
      "BOTH", "20",
      "MAC ACL", "30",
      "RADIUS", "40"
  );

  public static final Map<String, Integer> countries = ImmutableMap.of(
      "CZ", 10,
      "PL", 20
  );

  public static final Map<String, Integer> polarizations = ImmutableMap.of(
      "HORIZONTAL", 10,
      "VERTICAL", 20,
      "DUAL", 30
  );

  public static final Function<Object, Object> identityFn = (Object value) -> value;

  public static final Map<String, Function<Object, Object>> valueMappings = ImmutableMap.of(
      "type", nodeTypes::get,
      "mode", modeTypes::get,
      "auth", authentications::get,
      "country", countries::get,
      "polarization", polarizations::get
  );

  public static final Map<String, String> keyMappings = ImmutableMap.of(
      "linkTo", "linkto",
      "authentication", "auth"
  );

  public static Map<String, Object> mapNodeDetailToNode(Map<String, Object> map) {
    // add entry for 'mode', there are two entries from 'type' entry
    final Map<String, Object> updatedMap = new HashMap<>(map);
    if (map.containsKey("type")) {
      updatedMap.put("mode", map.get("type"));
    }

    final Map<String, Object> result = new HashMap<>();

    updatedMap.forEach((key, value) -> {
      final String mappedKey = keyMappings.getOrDefault(key, key);
      final Function<Object, Object> valueMapper = valueMappings.getOrDefault(mappedKey, identityFn);
      result.put(mappedKey, valueMapper.apply(value));
    });

    return result;
  }
}
