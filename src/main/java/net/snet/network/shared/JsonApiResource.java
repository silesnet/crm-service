package net.snet.network.shared;

import lombok.Value;
import lombok.experimental.Accessors;

import java.util.Map;

@Value
@Accessors(fluent = true)
public class JsonApiResource {
  String id;
  String type;
  Map<String, Object> attributes;
}
