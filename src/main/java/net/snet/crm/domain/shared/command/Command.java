package net.snet.crm.domain.shared.command;

import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.Entity;
import net.snet.crm.domain.shared.data.MapData;
import net.snet.crm.domain.shared.data.Record;
import org.joda.time.DateTime;

public class Command implements Entity<Command, CommandId>, Record {

  private final CommandId id;
  private final Commands name;
  private final String entity;
  private final long entityId;
  private final Data data;
  private final String status;
  private final Record record;

  public static Command of(Record record) {
    return new Command(record);
  }

  private Command(Record record) {
    final Data data = record.recordData();
    this.id = new CommandId(data.longOf("id"));
    this.name = Commands.of(data.stringOf("command"));
    this.entity = data.stringOf("entity");
    this.entityId = data.longOf("entity_id");
    this.data = MapData.of(data.mapOf("data"));
    this.status = data.stringOf("status");
    this.record = record;
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

  public long entityId() {
    return entityId;
  }

  public Data data() {
    return data;
  }

  public String status() {
    return status;
  }

  @Override
  public Data recordData() {
    return record.recordData();
  }
}
