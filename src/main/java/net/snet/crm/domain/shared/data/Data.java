package net.snet.crm.domain.shared.data;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

public interface Data
{
  Data EMPTY = MapData.of(ImmutableMap.<String, Object>of());

  boolean hasPath(String path);

  boolean hasValue(String path);

  boolean hasBoolean(String path);

  boolean hasNumber(String path);

  boolean hasDate(String path);

  boolean hasDateTime(String path);

  boolean hasMap(String path);

  boolean hasData(String path);

  boolean hasList(String path);

  boolean isEmpty();

  boolean booleanOf(String path);

  boolean optBooleanOf(String path, boolean def);

  boolean optBooleanOf(String path);

  int intOf(String path);

  int optIntOf(String path, int def);

  int optIntOf(String path);

  long longOf(String path);

  long optLongOf(String path, long def);

  long optLongOf(String path);

  String stringOf(String path);

  String optStringOf(String path, String def);

  String optStringOf(String path);

  DateTime dateTimeOf(String path);

  DateTime optDateTimeOf(String path, DateTime def);

  DateTime optDateTimeOf(String path);

  LocalDate dateOf(String path);

  LocalDate optDateOf(String path, LocalDate def);

  LocalDate optDateOf(String path);

  Data dataOf(String path);

  Data optDataOf(String path);

  Map<String, Object> mapOf(String path);

  Map<String, Object> optMapOf(String path);

  List<Object> listOf(String path);

  List<Object> optListOf(String path);

  Map<String, Object> asMap();

  Map<String, Object> asModifiableContent();

}
