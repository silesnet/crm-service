package net.snet.crm.service.utils;

import com.google.common.base.*;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Update;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.LongMapper;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.*;

public class Databases {
  private static String DRAFTS_TABLE = "drafts2";
  private static Pattern TABLE_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

  private static Function<String, String> columnToReference = columnToReference();
  private static Function<String, String> columnToAssignment = columnToAssignment();
  private static Function<Object, Object> valueToSqlType = valueToSqlType();
  private static Predicate<String> notId = notId();

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
    return insertStatement(table, record, handle)
        .executeAndReturnGeneratedKeys(LongMapper.FIRST).first();
  }

  public static void insertRecordWithoutKey(final String table,
                                            final Map<String, Object> record,
                                            final Handle handle) {
    final int insertedRows = insertStatement(table, record, handle).execute();
    checkState(insertedRows == 1, "failed to insert record into '%s'", table);
  }

  private static Update insertStatement(String table, Map<String, Object> record, Handle handle) {
    return handle
        .createStatement(insertSql(table, record.keySet()))
        .bindFromMap(Maps.transformValues(record, valueToSqlType));
  }

  public static Map<String, Object> getRecord(final String table,
                                              final long id,
                                              final Handle handle) {
    checkTableName(table);
    final Map<String, Object> record = handle
        .createQuery("SELECT * FROM " + table + " WHERE id=:id;")
        .bind("id", id)
        .first();
    checkNotNull(record, "can't get record '%s' from '%s' table", id, table);
    return record;
  }

  public static Optional<Map<String, Object>> getRecord(
      final String query,
      final Map<String, Object> binding,
      final DBI dbi) {
    return Optional.fromNullable(
        dbi.withHandle(new HandleCallback<Map<String, Object>>() {
          @Override
          public Map<String, Object> withHandle(Handle handle) throws Exception {
            return handle.createQuery(query)
                .bindFromMap(binding)
                .first();
          }
        }));
  }

  public static void updateRecord(final String table,
                                  final long id,
                                  final Map<String, Object> update,
                                  final Handle handle) {
    updateRecordWithId(new RecordId(table, "id", id), update, handle);
  }

  public static void updateRecordWithId(final RecordId recordId,
                                        final Map<String, Object> update,
                                        final Handle handle) {
    for (String column : update.keySet()) {
      if (update.get(column) instanceof Map) {
        final Map<String, Object> pgValue = (Map<String, Object>) update.get(column);
        if (pgValue.containsKey("type") && pgValue.containsKey("value")) {
          final PGobject value = new PGobject();
          value.setType(pgValue.get("type").toString());
          if (pgValue.get("value") != null && pgValue.get("value").toString().length() > 0)
          try {
            value.setValue(pgValue.get("value").toString());
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
          update.put(column, value);
        }
      }
    }
    final int updatedRows = handle
        .createStatement(updateSqlWithId(recordId.table(), recordId.idColumn(), update.keySet()))
        .bindFromMap(update)
        .bind(recordId.idColumn(), recordId.idValue())
        .execute();
    checkState(updatedRows == 1, "failed to update record '%s' in '%s'",
        recordId.idValue(), recordId.table());
  }

  public static long lastValueOf(final String table, final String column, final Handle handle) {
    checkTableName(table);
    checkColumnName(column);
    final Map<String, Object> last = handle
        .createQuery("SELECT " + column + " FROM " + table + " ORDER BY " + column + " DESC LIMIT 1")
        .first();
    if (last == null) return 0;
    return Long.valueOf(last.get(column).toString());
  }

  public static long nextValOf(final String sequence, final Handle handle) {
    checkColumnName(sequence);
    return handle
        .createQuery("SELECT nextval('" + sequence + "')")
        .map(LongMapper.FIRST)
        .first();
  }

  public static String insertSql(final String table, final Collection<String> columns) {
    checkTableName(table);
    final Collection<String> references = Collections2.transform(columns, columnToReference);
    return "INSERT INTO " + table + " (" + Joiner.on(", ").join(columns) +
        ") VALUES (" + Joiner.on(", ").join(references) + ");";
  }

  public static String updateSql(final String table,
                                 final Collection<String> columns) {
    return updateSqlWithId(table, "id", columns);
  }

  public static String updateSqlWithId(final String table,
                                       final String idColumn,
                                       final Collection<String> columns) {
    checkTableName(table);
    final List<String> assignments = FluentIterable.from(columns)
        .filter(notId)
        .transform(columnToAssignment)
        .toList();
    return "UPDATE " + table + " SET " + Joiner.on(", ").join(assignments)
        + " WHERE " + idColumn + "=:" + idColumn + ";";
  }

  private static Predicate<String> notId() {
    return new Predicate<String>() {
      @Override
      public boolean apply(@Nullable String input) {
        return !"id".equalsIgnoreCase(input);
      }
    };
  }

  private static Function<String, String> columnToReference() {
    return new Function<String, String>() {
      @Nullable
      @Override
      public String apply(@Nullable String column) {
        return ":" + column;
      }
    };
  }

  private static Function<String, String> columnToAssignment() {
    return new Function<String, String>() {
      @Nullable
      @Override
      public String apply(@Nullable String column) {
        return column + "=:" + column;
      }
    };
  }

  private static void checkTableName(final String table) {
    checkArgument(TABLE_NAME.matcher(table).matches(),
        "illegal table name provided '%s'", table);
  }

  private static void checkColumnName(final String column) {
    checkArgument(TABLE_NAME.matcher(column).matches(),
        "illegal column name provided '%s'", column);
  }


  private static Function<Object, Object> valueToSqlType() {
    return Functions.compose(optionalToValue(), dateTimeToTimestamp());
  }

  private static Function<Object, Object> valueAppType() {
    return timestampToDateTime();
  }

  private static Function<Object, Object> optionalToValue() {
    return new Function<Object, Object>() {
      @Nullable
      @Override
      public Object apply(@Nullable Object input) {
        Object output = input;
        if (input instanceof Optional) {
          final Optional optional = (Optional) input;
          output = optional.orNull();
        }
        return output;
      }
    };
  }

  private static Function<Object, Object> dateTimeToTimestamp() {
    return new Function<Object, Object>() {
      @Nullable
      @Override
      public Object apply(@Nullable Object input) {
        Object output = input;
        if (input instanceof DateTime) {
          final DateTime dateTime = (DateTime) input;
          output = new Timestamp(dateTime.getMillis());
        }
        return output;
      }
    };
  }

  private static Function<Object, Object> timestampToDateTime() {
    return new Function<Object, Object>() {
      @Nullable
      @Override
      public Object apply(@Nullable Object input) {
        Object output = input;
        if (input instanceof Timestamp) {
          final Timestamp timestamp = (Timestamp) input;
          output = new DateTime(timestamp);
        }
        return output;
      }
    };
  }

  public static class RecordId {
    final String table;
    final String idColumn;
    final Object idValue;

    public RecordId(String table, String idColumn, Object idValue) {
      this.table = table;
      this.idColumn = idColumn;
      this.idValue = idValue;
    }

    public String table() {
      return table;
    }

    public String idColumn() {
      return idColumn;
    }

    public Object idValue() {
      return idValue;
    }
  }
}
