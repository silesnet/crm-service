package net.snet.crm.domain.shared.event;

import net.snet.crm.domain.shared.Entity;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.Record;

public class Event implements Entity<Event, EventId>, Record {

  private final EventId id;
  private final Record record;

  public Event(Record record) {
    this.record = record;
    this.id = new EventId(record.recordData().longOf("id"));
  }

  @Override
  public EventId id() {
    return id;
  }

  @Override
  public Data recordData() {
    return record.recordData();
  }
}
