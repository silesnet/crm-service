package net.snet.crm.domain.shared.event;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import java.util.Set;

public class EventConstrain {
  private final EventId sinceId;
  private final Events event;
  private final String entity;
  private final long entityId;

  public static Builder builder() {
    return new Builder();
  }

  private EventConstrain(EventId sinceId, Events event, String entity, long entityId) {
    this.sinceId = sinceId;
    this.event = event;
    this.entity = entity;
    this.entityId = entityId;
  }

  public String sql() {
    final Set<String> constrains = Sets.newHashSet();
    if (sinceId.value() > 0) {
      constrains.add("id > " + sinceId.value());
    }
    if (entity != null && !entity.isEmpty()) {
      constrains.add("entity='" + entity + "'");
    }
    if (entity != null && !entity.isEmpty() && entityId > 0) {
      constrains.add("entity='" + entity + "'");
      constrains.add("entity_id=" + entityId);
    }
    if (event != null) {
      constrains.add("event='" + event.event() + "'");
    }
    return Joiner.on(" AND ").join(constrains);
  }

  public static class Builder {
    private long eventId = -1;
    private Events event = null;
    private String entity = null;
    private long entityId = -1;

    public EventConstrain build() {
      return new EventConstrain(new EventId(eventId), event, entity, entityId);
    }

    public Builder eventsPastEventId(long eventId) {
      this.eventId = eventId;
      return this;
    }

    public Builder forEvent(Events event) {
      this.event = event;
      return this;
    }

    public Builder forEntity(String entity) {
      this.entity = entity;
      this.entityId = -1;
      return this;
    }

    public Builder forEntityInstance(String entity, long entityId) {
      this.entity = entity;
      this.entityId = entityId;
      return this;
    }
  }
}
