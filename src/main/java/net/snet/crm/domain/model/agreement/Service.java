package net.snet.crm.domain.model.agreement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.shared.Entity;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
          .put("data", "data")
          .put("addressId", "address_id")
          .put("placeId", "place_id")
          .put("location", "location")
          .put("productId", "product_id")
          .put("additionalName", "additionalname")
          .build();

  private static final Map<String, Object> RECORD_DEFAULTS =
      ImmutableMap.<String, Object>builder()
          .put("additionalname", "")
          .put("frequency", 40)
          .put("info", "")
          .build();

  private static final ObjectMapper mapper = new ObjectMapper();

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

  private Map<String, Object> propsFromDraft( final Draft draft)
  {
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
    props.put("data", mapToJson(ImmutableMap.<String, Object>builder()
        .put("devices", devices(data))
        .build()));
    props.put("addressId", data.get("address_id").asLongOr(null));
    props.put("placeId", data.get("place_id").asLongOr(null));
    props.put("location", data.get("location_flat").asStringValueOr(""));
    props.put("productId", data.get("product").asInteger());
    props.put("additionalName", data.get("additional_name").asStringValueOr(""));
    return Collections.unmodifiableMap(props);
  }

  private List<Map<String, Object>> devices(ValueMap data) {
    final List<Map<String, Object>> result = new ArrayList<>();
    final Object devices = data.getRawOr("devices", null);
    if (devices instanceof Iterable) {
      final Iterable iterable = (Iterable) devices;
      for (Object device : iterable) {
        if (device instanceof Map) {
          final Map map = (Map) device;
          final Object name = map.get("name");
          if (name != null && name.toString().length() > 0) {
            result.add(ImmutableMap.<String, Object>builder()
                .put("name", name.toString())
                .put("owner", "" + map.get("owner"))
                .build());
          }
        }
      }
    }
    return result;
  }

  private Map<String, Object> recordFromProps() {
    final Map<String, Object> record = Maps.newLinkedHashMap();
    record.putAll(RECORD_DEFAULTS);
    record.putAll(recordOf(props, PROP_NAMES));
    return Collections.unmodifiableMap(record);
  }

  public long customerId() {
    return (Long) props.get("customerId");
  }

  public DateTime periodStart() {
    return (DateTime) props.get("periodStart");
  }

  private String mapToJson(Map<String, Object> map) {
    try {
      return mapper.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
