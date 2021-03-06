package net.snet.crm.domain.model.draft;

import net.snet.crm.domain.shared.data.Data;
import org.skife.jdbi.v2.Handle;

import java.util.List;
import java.util.Map;

public interface DraftRepository {

  long create(Map<String, Object> draft);

  Map<String, Object> get(long draftId);
  Data get(long draftId, Handle handle);

  Map<String, Object> getEntity(String entityType, long entityId);

  List<Map<String, Object>> findByStatus(String status);

  List<Map<String,Object>> findByOwnerAndStatus(String owner, String draft);

  String statusOf(long draftId, Handle handle);

  void update(long draftId, Map<String, Object> update);
  void update(long draftId, Data update, Handle handle);

  void updateStatusGraphOf(long draftId, String status, Handle handle);

  void delete(long draftId);
  void delete(long draftId, Handle handle);
}
