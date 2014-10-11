package net.snet.crm.service.dao;

import java.util.Map;

public interface DraftRepository {

  long createDraft(Map<String, Object> draft);

  Map<String, Object> get(long draftId);
}
