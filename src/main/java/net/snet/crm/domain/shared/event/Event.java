package net.snet.crm.domain.shared.event;

import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.shared.Entity;
import net.snet.crm.domain.shared.command.CommandId;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import net.snet.crm.domain.shared.data.MapRecord;
import net.snet.crm.domain.shared.data.Record;
import org.joda.time.DateTime;

import java.util.Map;

import static net.snet.crm.service.utils.JsonUtil.dataOf;
import static net.snet.crm.service.utils.JsonUtil.jsonOf;

public class Event implements Entity<Event, EventId>, Record {

  private final EventId id;
  private final Events name;
  private final String entity;
  private final long entityId;
  private final Data data;
  private final CommandId commandId;
  private final DateTime happenedOn;
  private final Record record;

  public static Event of(Record record) {
    return new Event(record);
  }

  public static EventWithName occurred(Events name) {
    return new EventWithName(name);
  }

  private Event(Record record) {
    final Data data = record.recordData();
    this.id = new EventId(data.longOf("id"));
    this.name = Events.of(data.stringOf("event"));
    this.entity = data.stringOf("entity");
    this.entityId = data.longOf("entity_id");
    this.data = dataOf(data.stringOf("data"));
    this.commandId = data.hasValue("command_id") ?
        new CommandId(data.longOf("command_id")) : CommandId.NONE;
    this.happenedOn = data.dateTimeOf("happened_on");
    this.record = record;
  }

  private Event(Events name, String entity, long entityId, Data data, CommandId commandId) {
    this.id = EventId.NONE;
    this.name = name;
    this.entity = entity;
    this.entityId = entityId;
    this.data = data;
    this.commandId = commandId;
    this.happenedOn = DateTime.now();
    this.record = MapRecord.of(ImmutableMap.<String, Object>builder()
        .put("id", this.id.value())
        .put("event", this.name.event())
        .put("entity", this.entity)
        .put("entity_id", this.entityId)
        .put("data", jsonOf(this.data))
        .put("command_id", this.commandId.value())
        .put("happened_on", this.happenedOn)
        .build());
  }

  @Override
  public EventId id() {
    return id;
  }

  public Events name() {
    return name;
  }

  public String entity() {
    return entity;
  }

  public long entityId() {
    return entityId;
  }

  public Data data() {
    return data;
  }

  public CommandId commandId() {
    return commandId;
  }

  public DateTime happenedOn() {
    return happenedOn;
  }

  @Override
  public Data recordData() {
    return record.recordData();
  }

  public static class EventWithName {
    private final Events name;

    private EventWithName(Events name) {
      this.name = name;
    }

    public EventBuilder on(String entity, long entityId) {
      return new EventBuilder(name, entity, entityId);
    }

    public EventBuilder on(String entity) {
      return new EventBuilder(name, entity, 0);
    }
  }

  public static final class EventBuilder {
    private final Events name;
    private final String entity;
    private final long entityId;
    private Data data = Data.EMPTY;
    private CommandId commandId = CommandId.NONE;

    private EventBuilder(Events name, String entity, long entityId) {
      this.name = name;
      this.entity = entity;
      this.entityId = entityId;
    }

    public EventBuilder withData(Data data) {
      this.data = data;
      return this;
    }

    public EventBuilder withData(Map<String, Object> data) {
      this.data = MapData.of(data);
      return this;
    }

    public EventBuilder withCommandId(CommandId commandId) {
      this.commandId = commandId;
      return this;
    }

    public EventBuilder withCommandId(long commandId) {
      this.commandId = new CommandId(commandId);
      return this;
    }

    public Event build() {
      return new Event(name, entity, entityId, data, commandId);
    }
  }

}
