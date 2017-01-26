package net.snet.crm.infrastructure.persistence.jdbi;

import net.snet.crm.domain.model.todo.TodoRepository;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.infrastructure.persistence.jdbi.support.DataMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.LongMapper;

import java.util.List;

public class DbiTodoRepository implements TodoRepository
{
  private final DBI dbi;

  public DbiTodoRepository(DBI dbi) {
    this.dbi = dbi;
  }

  @Override
  public List<Data> findServiceComments(final long serviceId) {
    return dbi.withHandle(new HandleCallback<List<Data>>()
    {
      @Override
      public List<Data> withHandle(Handle handle) throws Exception {
        return handle
            .createQuery(
                "SELECT * FROM service_comments WHERE customer_id=:customerId ORDER BY date DESC"
            )
            .bind("customerId", customerId(serviceId))
            .map(DataMapper.INSTANCE)
            .list();
      }
    });
  }

  @Override
  public Data findTodo(final long todoId) {
    return dbi.withHandle(new HandleCallback<Data>()
    {
      @Override
      public Data withHandle(Handle handle) throws Exception {
        return handle
            .createQuery("SELECT * FROM pltodo WHERE id=:todoId ORDER BY id desc")
            .bind("todoId", todoId)
            .map(DataMapper.INSTANCE)
            .first();
      }
    });
  }

  private long customerId(final long serviceId) {
    return dbi.withHandle(new HandleCallback<Long>()
    {
      @Override
      public Long withHandle(Handle handle) throws Exception {
        final Long customerId = handle
            .createQuery(
                "SELECT customer_id FROM services WHERE id=:serviceId"
            )
            .bind("serviceId", serviceId)
            .map(LongMapper.FIRST)
            .first();
        return customerId == null ? 0 : customerId;
      }
    });
  }
}
