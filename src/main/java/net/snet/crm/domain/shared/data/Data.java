package net.snet.crm.domain.shared.data;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public interface Data {
  Data EMPTY = MapData.of(ImmutableMap.<String, Object>of());

  boolean hasValue(String path);

  boolean hasPath(String path);

  boolean isEmpty();

  boolean booleanOf(String path);
  boolean optionalBooleanOf(String path, boolean def);
  boolean optionalBooleanOf(String path);

  int intOf(String path);
  int optionalIntOf(String path, int def);
  int optionalIntOf(String path);

  long longOf(String path);
  long optionalLongOf(String path, long def);
  long optionalLongOf(String path);

  String stringOf(String path);
  String optionalStringOf(String path, String def);
  String optionalStringOf(String path);

  DateTime dateTimeOf(String path);
  DateTime optionalDateTimeOf(String path, DateTime def);
  DateTime optionalDateTimeOf(String path);

  Data dataOf(String path);
  Data optionalDataOf(String path);

  Map<String, Object> mapOf(String path);
  Map<String, Object> optionalMapOf(String path);

  List<Object> listOf(String path);
  List<Object> optionalListOf(String path);

  Map<String, Object> asMap();

}
