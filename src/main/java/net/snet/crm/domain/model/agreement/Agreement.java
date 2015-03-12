package net.snet.crm.domain.model.agreement;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.shared.Entity;

import java.util.Collections;
import java.util.Map;

import static net.snet.crm.service.utils.Entities.entityOf;
import static net.snet.crm.service.utils.Entities.recordOf;
import static net.snet.crm.service.utils.Entities.valueOf;

public class Agreement implements Entity<Agreement, AgreementId> {
  private static final Map<String, String> PROP_NAMES =
      ImmutableMap.<String, String>builder()
          .put("id", "id")
          .put("country", "country")
          .put("customerId", "customer_id")
          .put("status", "status")
          .build();

  private static final Map<String, Object> RECORD_DEFAULTS =
      ImmutableMap.<String, Object>builder()
          .build();

  private final AgreementId id;
  private final Map<String, Object> props;
  private final Map<String, Object> record;

  public Agreement(Draft draft) {
    Preconditions.checkArgument(Draft.Entity.AGREEMENTS.equals(draft.entity()),
        "expected AGREEMENTS draft, but got %s", draft.entity());
    this.id = new AgreementId(draft.entityId());
    this.props = propsFromDraft(draft);
    this.record = recordFromProps();
  }

  public Agreement(Map<String, Object> record) {
    this.record = record;
    this.props = entityOf(record, PROP_NAMES);
    this.id = new AgreementId(valueOf("id", props, Long.class));
  }

  @Override
  public AgreementId id() {
    return id;
  }

  public Map<String, Object> props() {
    return props;
  }

  public Map<String, Object> record() {
    return record;
  }

  private Map<String, Object> propsFromDraft(final Draft draft) {
    final Map<String, Object> props = Maps.newLinkedHashMap();
    props.put("id", draft.entityId());
    for (Map.Entry<String, String> link : draft.links().entrySet()) {
      if (link.getKey().endsWith("customers")) {
        props.put("customerId", Long.valueOf(link.getValue()));
      }
    }
    props.put("country", draft.entitySpate());
    props.put("status", "ACTIVE");
    return Collections.unmodifiableMap(props);  }

  private Map<String, Object> recordFromProps() {
    final Map<String, Object> record = Maps.newLinkedHashMap();
    record.putAll(recordOf(props, PROP_NAMES));
    record.putAll(RECORD_DEFAULTS);
    return Collections.unmodifiableMap(record);  }

}
