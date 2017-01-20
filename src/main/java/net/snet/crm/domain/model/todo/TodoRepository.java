package net.snet.crm.domain.model.todo;

import java.util.List;
import java.util.Map;

public interface TodoRepository {
  List<Map<String, Object>> findServiceComments(long serviceId);
}
