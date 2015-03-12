package net.snet.crm.domain.model.draft;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.snet.crm.domain.shared.Entity;

import java.util.Map;

import static net.snet.crm.service.utils.Entities.*;

public class Draft implements Entity<Draft, DraftId> {

  public enum Entity { AGREEMENTS, SERVICES, CUSTOMERS, CONNECTIONS };
  public enum Status { DRAFT, SUBMITTED, APPROVED, IMPORTED }

  private static final Map<String, String> PROP_NAMES =
      ImmutableMap.<String, String>builder()
          .put("id", "id")
          .put("entityType", "entity_type")
          .put("entitySpate", "entity_spate")
          .put("entityId", "entity_id")
          .put("entityName", "entity_name")
          .put("status", "status")
          .put("owner", "owner")
          .put("data", "data")
          .put("links", "links")
          .build();

  private final Map<String, Object> props;
  private final DraftId id;
  private final Entity entity;
  private final String entitySpate;
  private final long entityId;
  private final Status status;
  private final Map<String, Object> data;
  private final Map<String, String> links;

  public Draft(Map<String, Object> record) {
    this.props = entityOf(record, PROP_NAMES);
    this.id = new DraftId(valueOf("id", props, Long.class));
    this.entity = Entity.valueOf(valueOf("entityType", props, String.class).toUpperCase());
    this.entitySpate = valueOf("entitySpate", props, String.class);
    this.entityId = valueOf("entityId", props, Long.class);
    this.status = Status.valueOf(valueOf("status", props, String.class).toUpperCase());
    this.data = mapOf("data", props);
    this.links = linksOf(optionalMapOf("links", props));
  }

  private Map<String, String> linksOf(Optional<Map<String, Object>> links) {
    final ImmutableMap.Builder<String, String> linksBuilder = ImmutableMap.builder();
    if (links.isPresent()) {
      for (Map.Entry<String, Object> link : links.get().entrySet()) {
        linksBuilder.put(link.getKey(), String.valueOf(link.getValue()));
      }
    }
    return linksBuilder.build();
  }

  @Override
  public DraftId id() {
    return id;
  }

  public Entity entity() {
    return entity;
  }

  public String entitySpate() {
    return entitySpate;
  }

  public long entityId() {
    return entityId;
  }

  public Status status() { return  status; }

  public Map<String, Object> data() {
    return data;
  }

  public Map<String, String> links() {
    return links;
  }

}
