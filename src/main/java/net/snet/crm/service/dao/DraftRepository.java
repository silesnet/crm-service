package net.snet.crm.service.dao;

import java.util.List;
import java.util.Map;

public interface DraftRepository {

  long create(Map<String, Object> draft);

  Map<String, Object> get(long draftId);

  Map<String, Object> getEntity(String entityType, long entityId);

  List<Map<String, Object>> findByStatus(String status);

  List<Map<String,Object>> findByOwnerAndStatus(String owner, String draft);

  void update(Map<String, Object> update);
}
