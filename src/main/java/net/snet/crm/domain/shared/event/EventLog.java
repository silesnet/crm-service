package net.snet.crm.domain.shared.event;

import java.util.List;

public interface EventLog {
  Event publish(Event event);

  Event find(EventId id);

  List<Event> eventsPast(EventId id, int batch);

  List<Event> eventsPast(EventId id, Events event, int batch);

  List<Event> eventsPast(EventId id, String entity, long entityId, int batch);


}
