package net.snet.crm.domain.shared.data;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;

import java.util.Map;

public interface Data {
  Data EMPTY = MapData.of(ImmutableMap.<String, Object>of());

  boolean hasValue(String path);

  boolean booleanOf(String path);
  boolean optionalBooleanOf(String path, boolean def);

  int intOf(String path);
  int optionalIntOf(String path, int def);

  long longOf(String path);
  long optionalLongOf(String path, long def);

  String stringOf(String path);
  String optionalStringOf(String path, String def);

  DateTime dateTimeOf(String path);
  DateTime optionalDateTimeOf(String path, DateTime def);

  Map<String, Object> mapOf(String path);
  Map<String, Object> asMap();

}
