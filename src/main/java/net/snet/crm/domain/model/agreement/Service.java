package net.snet.crm.domain.model.agreement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.shared.Entity;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static net.snet.crm.service.utils.Entities.*;

public class Service implements Entity<Service, ServiceId> {
  private static final Map<String, String> PROP_NAMES =
      ImmutableMap.<String, String>builder()
          .put("id", "id")
          .put("customerId", "customer_id")
          .put("periodStart", "period_from")
          .put("periodEnd", "period_to")
          .put("productName", "name")
          .put("chargingAmount", "price")
          .put("connectionDownload", "download")
          .put("connectionUpload", "upload")
          .build();

  private static final Map<String, Object> RECORD_DEFAULTS =
      ImmutableMap.<String, Object>builder()
        .put("additionalname", "")
        .put("frequency", 40)
        .put("bps", "M")
        .put("is_aggregated", false)
        .put("info", "")
        .build();

  public static final DateTimeFormatter DRAFT_DATE_FORMATTER = DateTimeFormat.forPattern("dd.MM.YYYY");

  private final ServiceId id;
  private final Map<String, Object> props;
  private final Map<String, Object> record;

  public Service(Draft draft) {
    checkArgument(Draft.Entity.SERVICES.equals(draft.entity()),
        "expected SERVICES draft, but got %s", draft.entity());
    this.id = new ServiceId(draft.entityId());
    this.props = propsFromDraft(draft);
    this.record = recordFromProps();
  }

  public Service(Map<String, Object> record) {
    this.props = entityOf(record, PROP_NAMES);
    this.id = new ServiceId(valueOf("id", props, Long.class));
    this.record = record;
  }

  @Override
  public ServiceId id() {
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
      if (link.getKey().endsWith("agreements")) {
        props.put("agreementId", Long.valueOf(link.getValue()));
      }
    }
//    final Map<String, Object> data = draft.data();
    final ValueMap data = valueMapOf(draft.data());
    final DateTime periodStart = DRAFT_DATE_FORMATTER
        .parseDateTime(String.valueOf(data.get("activate_on")));
    props.put("periodStart", periodStart);
    props.put("periodEnd", null);
    props.put("productName", data.get("product_name").toString());
    props.put("chargingAmount", data.get("price").asIntegerOr(0));
    props.put("connectionDownload", data.get("downlink").asIntegerOr(null));
    props.put("connectionUpload", data.get("uplink").asIntegerOr(null));
    return Collections.unmodifiableMap(props);
  }

  private Map<String, Object> recordFromProps() {
    final Map<String, Object> record = Maps.newLinkedHashMap();
    record.putAll(recordOf(props, PROP_NAMES));
    record.putAll(RECORD_DEFAULTS);
    return Collections.unmodifiableMap(record);
  }

  public long customerId() {
    return (Long) props.get("customerId");
  }

  public DateTime periodStart() {
    return (DateTime) props.get("periodStart");
  }
}
