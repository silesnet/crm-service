package net.snet.crm.domain.shared.event;

import net.snet.crm.domain.shared.data.Data;

import java.util.List;

public interface EventLog {
  Event publish(String event, Data data);

  Event find(EventId id);

  List<Event> eventsPast(EventId id, int batch);
}
