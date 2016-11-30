package net.snet.crm.infrastructure.persistence.jdbi;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import net.snet.crm.domain.shared.data.MapRecord;
import net.snet.crm.domain.shared.data.Record;
import net.snet.crm.domain.shared.event.*;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.tweak.HandleCallback;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.snet.crm.service.utils.Databases.findRecords;
import static net.snet.crm.service.utils.Databases.getRecord;
import static net.snet.crm.service.utils.Databases.insertRecord;

public class DbiEventLog implements EventLog {
  private static final String EVENTS = "events";
  private static final Function<Map<String, Object>, Event> mapToEvent = mapToEvent();

  private final DBI dbi;

  public DbiEventLog(DBI dbi) {
    this.dbi = dbi;
  }

  @Override
  public Event publish(final Event event) {
    return dbi.inTransaction(new TransactionCallback<Event>() {
      @Override
      public Event inTransaction(Handle handle, TransactionStatus status) throws Exception {
        final Data values = values(event);
        final long id = insertRecord(EVENTS, values.asMap(), handle);
        return Event.of(record(values, id));
      }
    });
  }

  @Override
  public Event find(final EventId id) {
    return dbi.withHandle(new HandleCallback<Event>() {
      @Override
      public Event withHandle(Handle handle) throws Exception {
        return mapToEvent.apply(getRecord(EVENTS, id.value(), handle));
      }
    });
  }

  public List<Event> eventsPast(final EventId id, int batch) {
    return FluentIterable
        .from(findRecords("SELECT * FROM events WHERE id>:id ORDER BY id LIMIT " + batch,
            ImmutableMap.<String, Object>of("id", id.value()), dbi))
        .transform(mapToEvent).toList();
  }

  public List<Event> eventsPast(EventId id, Events event, int batch) {
    return FluentIterable
        .from(findRecords("SELECT * FROM events WHERE id>:id AND event=:event ORDER BY id LIMIT " + batch,
            ImmutableMap.<String, Object>of("id", id.value(), "event", event.event()), dbi))
        .transform(mapToEvent).toList();
  }

  public List<Event> eventsPast(EventId id, String entity, long entityId, int batch) {
    return FluentIterable
        .from(findRecords("SELECT * FROM events WHERE id>:id AND entity=:entity AND entity_id=:entityId ORDER BY id LIMIT " + batch,
            ImmutableMap.<String, Object>of("id", id.value(), "entity", entity, "entityId", entityId), dbi))
        .transform(mapToEvent).toList();
  }

  @Override
  public List<Event> events(EventConstrain constrain) {
    return Lists.newArrayList();
  }

  private Data values(Event event) {
    final Map<String, Object> values = event.recordData().asMap();
    values.remove("id");
    return MapData.of(values);
  }

  private Record record(Data values, long id) {
    final HashMap<String, Object> event = Maps.newHashMap(values.asMap());
    event.put("id", id);
    return MapRecord.of(event);
  }

  private static Function<Map<String, Object>, Event> mapToEvent() {
    return new Function<Map<String, Object>, Event>() {
      @Nullable
      @Override
      public Event apply(@Nullable Map<String, Object> map) {
        return Event.of(MapRecord.of(map));
      }
    };
  }

}
