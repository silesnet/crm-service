package net.snet.crm.domain.shared.event;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

public class EventConstrain {
  private final String sql;
  private final Map<String, Object> binding;

  public static Builder builder() {
    return new Builder();
  }

  private EventConstrain(EventId sinceId, Events event, String entity, long entityId) {
    this.binding = Maps.newHashMap();

    final Set<String> constrains = Sets.newHashSet();
    if (sinceId.value() >= 0) {
      constrains.add("id > :id");
      binding.put("id", sinceId.value());
    }
    if (entity != null && !entity.isEmpty()) {
      constrains.add("entity = :entity");
      binding.put("entity", entity);
    }
    if (entity != null && !entity.isEmpty() && entityId > 0) {
      constrains.add("entity = :entity");
      constrains.add("entity_id = :entity_id");
      binding.put("entity", entity);
      binding.put("entity_id", entityId);
    }
    if (event != null) {
      constrains.add("event = :event");
      binding.put("event", event.event());
    }
    sql = Joiner.on(" AND ").join(constrains);
  }

  public Map<String, Object> binding() {
    return binding;
  }

  public String sql() {
    return sql;
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
      checkArgument(eventId >= 0, "event id can't be negative, was '%s'", eventId);
      this.eventId = eventId;
      return this;
    }

    public Builder forEvent(Events event) {
      checkArgument(event != null, "event can't be null");
      this.event = event;
      return this;
    }

    public Builder forEntity(String entity) {
      checkArgument(entity != null, "entity can't be null");
      this.entity = entity;
      this.entityId = -1;
      return this;
    }

    public Builder forEntityInstance(String entity, long entityId) {
      checkArgument(entity != null, "entity can't be null");
      checkArgument(entityId > 0, "entity id must be larger than zero, was '%s'", entityId);
      this.entity = entity;
      this.entityId = entityId;
      return this;
    }
  }
}
