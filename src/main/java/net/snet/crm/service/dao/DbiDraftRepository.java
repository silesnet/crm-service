package net.snet.crm.service.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.*;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static net.snet.crm.service.utils.Databases.getRecord;
import static net.snet.crm.service.utils.Databases.insertRecord;
import static net.snet.crm.service.utils.Entities.*;

public class DbiDraftRepository implements DraftRepository {
  private static final Logger logger =
      LoggerFactory.getLogger(DbiDraftRepository.class);
  public static final String DRAFTS_TABLE = "drafts2";
  private static final Map<String, String> draftFields =
      ImmutableMap.<String, String>builder()
          .put("id", "id")
          .put("user", "user")
          .put("entityType", "entity_type")
          .put("entityId", "entity_id")
          .put("entityName", "entity_name")
          .put("status", "status")
          .put("data", "data")
          .build();

  private final DBI dbi;
  private final ObjectMapper objectMapper;

  public DbiDraftRepository(final DBI dbi, ObjectMapper objectMapper) {
    this.dbi = dbi;
    this.objectMapper = objectMapper;
  }

  @Override
  public long createDraft(final Map<String, Object> draft) {
    logger.debug("creating draft");
    final Map<String, Object> record = recordOf(draft, draftFields);
    record.put("data", toJson(record.get("data")));
    // TODO set correct entityId for the draft
    return dbi.withHandle(new HandleCallback<Long>() {
      @Override
      public Long withHandle(Handle handle) throws Exception {
        return insertRecord(DRAFTS_TABLE, record, handle);
      }
    });
  }

  @Override
  public Map<String, Object> get(final long draftId) {
    logger.debug("getting draft '{}'", draftId);
    return dbi.withHandle(new HandleCallback<Map<String, Object>>() {
      @Override
      public Map<String, Object> withHandle(Handle handle) throws Exception {
        return entityOf(getRecord(DRAFTS_TABLE, draftId, handle), draftFields);
      }
    });
  }

  private String toJson(Object data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
