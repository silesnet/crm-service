package net.snet.crm.service.dao;

import org.skife.jdbi.v2.DBI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbiTodoRepository implements TodoRepository {
  private final DBI dbi;

  public DbiTodoRepository(DBI dbi) {
    this.dbi = dbi;
  }

  @Override
  public List<Map<String, Object>> findServiceComments(long serviceId) {
    return new ArrayList<>();
  }
}
