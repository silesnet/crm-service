package net.snet.crm.service.utils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongMapper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Databases {
  private static String DRAFTS_TABLE = "drafts2";
  private static Pattern TABLE_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

  private static Function<String, String> idToNull = idToNull();
  private static Function<String, String> toValueReference = toValueReference();

  public static long lastEntityIdFor(final String entityType,
                                     final String entitySpate,
                                     final String entityConstrain,
                                     final Handle handle) {

    checkTableName(entityType);
    return handle
        .createQuery(
            "SELECT max(max_id) FROM (" +
                "SELECT MAX(id) max_id FROM " + entityType +
                " WHERE " + entityConstrain + " " +
                "UNION ALL " +
                "SELECT MAX(entity_id) max_id FROM " + DRAFTS_TABLE + " WHERE " +
                "entity_type=:entity_type AND entity_spate=:entity_spate" +
                ") sub_query"
        )
        .bind("entity_type", entityType)
        .bind("entity_spate", entitySpate)
        .map(LongMapper.FIRST)
        .first();
  }

  public static long lastEntityIdFor(final String entityType,
                                     final String entitySpate,
                                     final Handle handle) {
    return lastEntityIdFor(entityType, entitySpate, "1=1", handle);
  }


  public static long insertRecord(final String table,
                                  final Map<String, Object> record,
                                  final Handle handle) {
    return (long) handle
        .createStatement(insertSql(table, record.keySet()))
        .bindFromMap(record)
        .execute();
  }

  public static Map<String, Object> getRecord(final String table,
                                              final long id,
                                              final Handle handle) {
    checkTableName(table);
    final Map<String, Object> record = handle
        .createQuery("SELECT * FROM " + table + " WHERE id=:id;")
        .bind("id", id)
        .first();
    checkNotNull(record,
        "can't get record '%s' from '%s' table", id, table);
    return record;
  }

  public static String insertSql(final String table,
                                 final Collection<String> columns) {
    return insertSql(table, columns, idToNull, Predicates.<String>notNull());
  }

  private static String insertSql(final String table,
                                  final Collection<String> rawColumns,
                                  final Function<String, String> updateName,
                                  final Predicate<String> filterColumn) {
    checkTableName(table);
    final List<String> columns = FluentIterable.from(rawColumns)
        .transform(updateName)
        .filter(filterColumn)
        .toList();
    final List<String> references = FluentIterable.from(columns)
        .transform(toValueReference).toList();
    return "INSERT INTO " + table + " (" + Joiner.on(", ").join(columns) +
           ") VALUES (" + Joiner.on(", ").join(references) + ");";
  }

  private static Function<String, String> idToNull() {
    return new Function<String, String>() {
      @Nullable
      @Override
      public String apply(@Nullable String column) {
        return "id".equalsIgnoreCase(column) ? null : column;
      }
    };
  }

  private static Function<String, String> toValueReference() {
    return new Function<String, String>() {
      @Nullable
      @Override
      public String apply(@Nullable String column) {
        return ":" + column;
      }
    };
  }

  private static void checkTableName(final String table) {
    checkArgument(TABLE_NAME.matcher(table).matches(),
        "illegal table name provided '%s'", table);
  }

}
