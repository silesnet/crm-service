package net.snet.crm.service.dao;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.LongMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbiTodoRepository implements TodoRepository {
  private final DBI dbi;

  public DbiTodoRepository(DBI dbi) {
    this.dbi = dbi;
  }

  @Override
  public List<Map<String, Object>> findServiceComments(final long serviceId) {
    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        return handle.createQuery(
            "SELECT * FROM service_comments WHERE customer_id=:customerId ORDER BY date DESC")
            .bind("customerId", customerId(serviceId))
            .list();
      }
    });
  }

  private long customerId(final long serviceId) {
    return dbi.withHandle(new HandleCallback<Long>() {
      @Override
      public Long withHandle(Handle handle) throws Exception {
        final Long customerId = handle.createQuery(
            "SELECT customer_id FROM services WHERE id=:serviceId")
            .bind("serviceId", serviceId)
            .map(LongMapper.FIRST)
            .first();
        return customerId == null ? 0 : customerId;
      }
    });
  }
}
