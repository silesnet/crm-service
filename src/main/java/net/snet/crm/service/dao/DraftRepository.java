package net.snet.crm.service.dao;

import java.util.List;
import java.util.Map;

public interface DraftRepository {

  long createDraft(Map<String, Object> draft);

  Map<String, Object> get(long draftId);

  Map<String, Object> getByType(String entityType, long entityId);

  List<Map<String, Object>> findDraftsByStatus(String status);

  List<Map<String,Object>> findDraftsByOwnerAndStatus(String owner, String draft);
}
