package net.snet.crm.domain.model.agreement;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.shared.Entity;

import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static net.snet.crm.service.utils.Entities.*;

public class Customer implements Entity<Customer, CustomerId> {
  private static final Map<String, String> PROP_NAMES =
      ImmutableMap.<String, String>builder()
          .put("id", "id")
          .put("name", "name")
          .put("nameExtra", "supplementary_name")
          .put("addressStreet", "street")
          .put("addressTown", "city")
          .put("addressPostalCode", "postal_code")
          .put("addressCountryId", "country")
          .put("contactName", "contact_name")
          .put("contactEmail", "email")
          .put("contactPhone", "phone")
          .put("publicId", "public_id")
          .put("taxId", "dic")
          .put("otherInfo", "info")
          .build();

  private static final Map<String, Object> RECORD_DEFAULTS =
      ImmutableMap.<String, Object>builder()
          .put("history_id", Optional.absent()) // would be populated on insert
          .put("contract_no", "")
          .put("connection_spot", "")
          .put("inserted_on", Optional.absent()) // would be populated on insert
          .put("frequency", 40)
          .put("lastly_billed", Optional.absent())
          .put("deliver_by_email", true)
          .put("deliver_copy_email", "")
          .put("deliver_by_mail", false)
          .put("status", 10)
          .put("shire_id", Optional.absent())
          .put("format", 10)
          .put("deliver_signed", false)
          .put("symbol", "")
          .put("updated", Optional.absent()) // would be populated on update and insert
          .put("account_no", Optional.absent())
          .put("bank_no", Optional.absent())
          .put("variable", Optional.absent())
          .put("customer_status", "CREATED")
          .put("is_billed_after", false)
          .put("is_auto_billing", true)
          .put("is_active", false)
          .build();

  final CustomerId id;
  private final Map<String, Object> props;
  private final Map<String, Object> record;

  public Customer(Draft draft) {
    checkArgument(Draft.Entity.CUSTOMERS.equals(draft.entity()),
        "expected CUSTOMERS draft, but got %s", draft.entity());
    this.id = new CustomerId(draft.entityId());
    this.props = propsFromDraft(draft);
    this.record = recordFromProps();  }

  public Customer(Map<String, Object> record) {
    this.props = entityOf(record, PROP_NAMES);
    this.id = new CustomerId(valueOf("id", props, Long.class));
    this.record = record;
  }

  @Override
  public CustomerId id() {
    return id;
  }

  public boolean isResidential() {
    return "".equals(props.get("taxId")) || props.get("taxId") == null;
  }

  public boolean isBusiness() {
    return !isResidential();
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
    final ValueMap data = valueMapOf(draft.data());
    final boolean isBusiness = "2".equals(data.get("customer_type").asString());
    if (isBusiness) {
      props.put("name", data.getRaw("supplementary_name"));
      props.put("nameExtra", data.getRaw("representative"));
    } else {
      props.put("name", Joiner.on(" ").skipNulls()
          .join(data.get("surname").asStringOr(null), data.get("name").asStringOr(null)));
      props.put("nameExtra", "");
    }
    props.put("addressStreet", streetAddress(
        data.getRaw("street"),
        data.getRaw("descriptive_number"),
        data.getRaw("orientation_number")));
    props.put("addressTown", data.getRaw("town"));
    props.put("addressPostalCode", data.getRaw("postal_code"));
    props.put("addressCountryId", data.get("country").asLong());
    props.put("contactName", data.getRaw("contact_name"));
    props.put("contactEmail", data.getRaw("email"));
    props.put("contactPhone", data.getRaw("phone"));
    props.put("publicId", data.getRaw("public_id"));
    if (isBusiness) {
      props.put("taxId", data.getRawOr("dic", data.getRawOr("public_id", System.currentTimeMillis())));
    } else {
      props.put("taxId", "");
    }
    props.put("otherInfo", data.getRaw("info"));
    return Collections.unmodifiableMap(props);
  }

  private Map<String, Object> recordFromProps() {
    final Map<String, Object> record = Maps.newLinkedHashMap();
    record.putAll(recordOf(props, PROP_NAMES));
    record.putAll(RECORD_DEFAULTS);
    return Collections.unmodifiableMap(record);
  }

  private String streetAddress(Object name, Object descriptiveNumber, Object orientationNumber) {
    return Joiner.on(" ").skipNulls().join(
        asStringOrNull(name),
        Joiner.on("/").skipNulls().join(
            asStringOrNull(descriptiveNumber),
            asStringOrNull(orientationNumber)
        )
    );
  }

  private String asStringOrNull(Object obj) {
    return Strings.emptyToNull(Optional.fromNullable(obj).or("").toString());
  }

}
