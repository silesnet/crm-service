package net.snet.crm.infrastructure.persistence.jdbi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.snet.crm.domain.shared.command.Command;
import net.snet.crm.domain.shared.command.CommandId;
import net.snet.crm.domain.shared.command.CommandQueue;
import net.snet.crm.domain.shared.command.Commands;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapRecord;
import net.snet.crm.domain.shared.data.Record;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.tweak.HandleCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.snet.crm.service.utils.Databases.*;

public class DbiCommandQueue implements CommandQueue {
  public static final String COMMANDS_TABLE = "commands";
  private final DBI dbi;
  private final ObjectMapper mapper;


  public DbiCommandQueue(DBI dbi, ObjectMapper mapper) {
    this.dbi = dbi;
    this.mapper = mapper;
  }

  @Override
  public Command submit(Commands command, String entity, String entityId, Data data) {
    final Map<String, Object> record = ImmutableMap.<String, Object>builder()
        .put("command", command.command())
        .put("entity", entity)
        .put("entity_id", entityId)
        .put("data", toJsonString(data))
        .put("status", "issued")
        .put("inserted_on", DateTime.now())
        .build();

    return dbi.withHandle(new HandleCallback<Command>() {
      @Override
      public Command withHandle(Handle handle) throws Exception {
        final long id = insertRecord(COMMANDS_TABLE, record, handle);
        final Record commandRecord = MapRecord.of(getRecord(COMMANDS_TABLE, id, handle));
        return new Command(commandRecord);
      }
    });
  }

  @Override
  public List<Command> nextOf(final Commands command, final int batch) {
    final List<Map<String, Object>> commands = findRecords(
        "SELECT * FROM " + COMMANDS_TABLE + " WHERE " +
            "command=:command AND status='issued' ORDER BY id LIMIT " + batch,
        ImmutableMap.<String, Object>of("command", command.command()),
        dbi);
    final List<Command> result = new ArrayList<>(commands.size());
    for (Map<String, Object> row : commands) {
      result.add(new Command(MapRecord.of(row)));
    }
    return result;
  }

  @Override
  public Command process(final CommandId id) {
    return dbi.inTransaction(new TransactionCallback<Command>() {
      @Override
      public Command inTransaction(Handle handle, TransactionStatus status) throws Exception {
        final Record currentRecord = MapRecord.of(getRecord(COMMANDS_TABLE, id.value(), handle));
        final Command current = new Command(currentRecord);
        if ("started".equals(current.status()) || "completed".equals(current.status())) {
          throw new IllegalStateException("command '" + id.value() + "' is already in '" +
            current.status() + "' state");
        }
        final Map<String, Object> update = Maps.newHashMap();
        update.put("status", "started");
        update.put("started_on", DateTime.now());
        update.put("finished_on", null);
        updateRecord(COMMANDS_TABLE, id.value(), update, handle);
        final Record updatedRecord = MapRecord.of(getRecord(COMMANDS_TABLE, id.value(), handle));
        final Command updated = new Command(updatedRecord);
        return updated;
      }
    });
  }

  @Override
  public Command complete(final CommandId id) {
    return dbi.inTransaction(new TransactionCallback<Command>() {
      @Override
      public Command inTransaction(Handle handle, TransactionStatus status) throws Exception {
        final Record currentRecord = MapRecord.of(getRecord(COMMANDS_TABLE, id.value(), handle));
        final Command current = new Command(currentRecord);
        if (!"started".equals(current.status())) {
          throw new IllegalStateException("command '" + id.value() + "' has not 'started'");
        }
        updateRecord(COMMANDS_TABLE, id.value(),
            ImmutableMap.<String, Object>of("status", "completed", "finished_on", DateTime.now()), handle);
        final Record updatedRecord = MapRecord.of(getRecord(COMMANDS_TABLE, id.value(), handle));
        final Command updated = new Command(updatedRecord);
        return updated;
      }
    });
  }

  @Override
  public Command fail(final CommandId id) {
    return dbi.inTransaction(new TransactionCallback<Command>() {
      @Override
      public Command inTransaction(Handle handle, TransactionStatus status) throws Exception {
        final Record currentRecord = MapRecord.of(getRecord(COMMANDS_TABLE, id.value(), handle));
        final Command current = new Command(currentRecord);
        if (!"started".equals(current.status())) {
          throw new IllegalStateException("command '" + id.value() + "' has not 'started'");
        }
        updateRecord(COMMANDS_TABLE, id.value(),
            ImmutableMap.<String, Object>of("status", "failed", "finished_on", DateTime.now()), handle);
        final Record updatedRecord = MapRecord.of(getRecord(COMMANDS_TABLE, id.value(), handle));
        final Command updated = new Command(updatedRecord);
        return updated;
      }
    });
  }

  private String toJsonString(Data data) {
    try {
      return mapper.writeValueAsString(data.asMap());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
