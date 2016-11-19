package net.snet.crm.domain.shared.event;

import net.snet.crm.domain.shared.GenericLongId;

public class EventId extends GenericLongId<EventId> {
  public static EventId NONE = new EventId();

  public EventId(long id) {
    super(id);
  }
  private EventId() {super();}
}
