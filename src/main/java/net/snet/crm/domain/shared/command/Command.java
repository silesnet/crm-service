package net.snet.crm.domain.shared.command;

import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.Entity;
import net.snet.crm.domain.shared.data.MapData;
import net.snet.crm.domain.shared.data.Record;
import org.joda.time.DateTime;

public class Command implements Entity<Command, CommandId> {

  private final CommandId id;
  private final Commands name;
  private final String entity;
  private final String entityId;
  private final Data data;
  private final String status;

  private final Record record;

  public Command(Record record) {
    this.record = record;
    final Data data = record.recordData();
    this.id = new CommandId(data.longOf("id"));
    this.name = Commands.of(data.stringOf("command"));
    this.entity = data.stringOf("entity");
    this.entityId = data.stringOf("entity_id");
    this.data = MapData.of(data.mapOf("data"));
    this.status = data.stringOf("status");
  }

  @Override
  public CommandId id() {
    return id;
  }

  public Commands name() {
    return name;
  }

  public String entity() {
    return entity;
  }

  public String entityId() {
    return entityId;
  }

  public Data data() {
    return data;
  }

  public String status() {
    return status;
  }
}
