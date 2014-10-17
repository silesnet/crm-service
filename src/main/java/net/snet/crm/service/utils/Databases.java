package net.snet.crm.service.utils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.skife.jdbi.v2.Handle;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Databases {
  private static Function<String, String> idToNull = idToNull();
  private static Function<String, String> toValueReference = toValueReference();

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

}
