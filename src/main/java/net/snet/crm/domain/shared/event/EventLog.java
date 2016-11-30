package net.snet.crm.domain.shared.event;

import java.util.List;

public interface EventLog {
  Event publish(Event event);

  Event find(EventId id);

  List<Event> events(EventConstrain constrain);
}
