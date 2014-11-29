package net.snet.crm.service.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.util.LongMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.service.dao.EntityIdFactory.entityIdFor;
import static net.snet.crm.service.utils.Entities.*;
import static net.snet.crm.service.utils.Databases.*;

public class DbiDraftRepository implements DraftRepository {
  private static final Logger logger =
      LoggerFactory.getLogger(DbiDraftRepository.class);
  public static final String DRAFTS_TABLE = "drafts2";
  public static final String DRAFTS_LINKS_TABLE = "draft_links";
  private static final Map<String, String> DRAFT_FIELDS =
      ImmutableMap.<String, String>builder()
          .put("id", "id")
          .put("entityType", "entity_type")
          .put("entitySpate", "entity_spate")
          .put("entityId", "entity_id")
          .put("entityName", "entity_name")
          .put("status", "status")
          .put("owner", "owner")
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
    final Map<String, Object> record = recordOf(draft, DRAFT_FIELDS);
	  final Optional<String> entityType = fetchNested("entity_type", record, String.class);
    checkState(entityType.isPresent(), "entity type was not provided");
    final Optional<String> entitySpate = fetchNested("entity_spate", record, String.class);
    checkState(entitySpate.isPresent(), "entity spate was not provided");
    logger.debug("entity type.spate: {}.{}", entityType.get(), entitySpate.get());
    record.put("data", toJson(record.get("data")));
    final Optional<Map<String, Object>> links = fetchNestedMap("links", draft);
    return dbi.withHandle(new HandleCallback<Long>() {
      @Override
      public Long withHandle(Handle handle) throws Exception {
        final long draftId;
        final Optional<Long> availableDraftId = availableDraftIdOfType(entityType.get(), handle);
        if (availableDraftId.isPresent()) {
          draftId = availableDraftId.get();
          updateRecord(DRAFTS_TABLE, draftId, record, handle);
        } else {
          final EntityId entityId =
              entityIdFor(entityType.get(), entitySpate.get(), handle);
          record.put("entity_id", entityId.nextId());
          draftId = insertRecord(DRAFTS_TABLE, record, handle);
        }
        if (links.isPresent()) {
          for (String entity : links.get().keySet()) {
            final Long linkedEntityId =
                Long.valueOf(links.get().get(entity).toString());
            final Map<String, Object> linkRecord =
                ImmutableMap.of("draft_id", draftId, "entity", entity,
                    "entity_id", (Object) linkedEntityId);
            insertRecordWithoutKey(DRAFTS_LINKS_TABLE, linkRecord, handle);
          }
        }
        return draftId;
      }
    });
  }

  @Override
  public Map<String, Object> get(final long draftId) {
    logger.debug("getting draft '{}'", draftId);
    return dbi.withHandle(new HandleCallback<Map<String, Object>>() {
      @Override
      public Map<String, Object> withHandle(Handle handle) throws Exception {
        return getByDraftId(draftId, handle);
      }
    });
  }

  @Override
  public Map<String, Object> getByType(final String entityType,
                                       final long entityId) {
    logger.debug("getting draft by type '{}/{}'", entityType, entityId);
    return dbi.withHandle(new HandleCallback<Map<String, Object>>() {
      @Override
      public Map<String, Object> withHandle(Handle handle) throws Exception {
        final Optional<Long> draftId = draftIdOfType(entityType, entityId, handle);
        checkState(draftId.isPresent(),
            "can't find draft by entity '%s/%s'", entityType, entityId);
        return getByDraftId(draftId.get(), handle);
      }
    });
  }

  @Override
  public List<Map<String, Object>> findDraftsByStatus(final String status) {
    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        final List<Map<String, Object>> draftsData = handle
            .createQuery("SELECT * FROM " + DRAFTS_TABLE + " WHERE status=:status;")
            .bind("status", status)
            .list();
        final List<Map<String, Object>> drafts = Lists.newArrayList();
        for (Map<String, Object> record : draftsData) {
          final Map<String, Object> draft = entityOf(record, DRAFT_FIELDS);
          final Long draftId = Long.valueOf(draft.get("id").toString());
          final Map<String, Object> links = draftLinks(draftId, handle);
          if (!links.isEmpty()) {
            draft.put("links", links);
          }
          drafts.add(draft);
        }
        return drafts;
      }
    });
  }

  @Override
  public List<Map<String, Object>> findDraftsByOwnerAndStatus(
                                                      final String owner,
                                                      final String status) {

    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>() {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception {
        final List<Map<String, Object>> draftsData = handle
            .createQuery("SELECT * FROM " + DRAFTS_TABLE +
                " WHERE owner=:owner AND status=:status;")
            .bind("owner", owner)
            .bind("status", status)
            .list();
        final List<Map<String, Object>> drafts = Lists.newArrayList();
        for (Map<String, Object> record : draftsData) {
          final Map<String, Object> draft = entityOf(record, DRAFT_FIELDS);
          final Long draftId = Long.valueOf(draft.get("id").toString());
          final Map<String, Object> links = draftLinks(draftId, handle);
          if (!links.isEmpty()) {
            draft.put("links", links);
          }
          drafts.add(draft);
        }
        return drafts;
      }
    });
  }

  private Map<String, Object> getByDraftId(final long draftId,
                                           final Handle handle) {
    final Map<String, Object> entity =
        entityOf(getRecord(DRAFTS_TABLE, draftId, handle), DRAFT_FIELDS);
    final Map<String, Object> links = draftLinks(draftId, handle);
    if (!links.isEmpty()) {
      entity.put("links", links);
    }
    return entity;
  }

  private Optional<Long> availableDraftIdOfType(final String entityType,
                                                final Handle handle) {
    final Long availableId = handle.createQuery(
        "SELECT id FROM " + DRAFTS_TABLE +
            " WHERE status='AVAILABLE' AND " + "entity_type=:entity_type" +
            " ORDER BY id")
        .bind("entity_type", entityType)
        .map(LongMapper.FIRST)
        .first();
    return Optional.fromNullable(availableId);
  }

  private Optional<Long> draftIdOfType(final String entityType,
                                       final long entityId,
                                       final Handle handle) {
    final Long availableId = handle.createQuery(
        "SELECT id FROM " + DRAFTS_TABLE +
            " WHERE entity_type=:entity_type AND entity_id=:entity_id" +
            " ORDER BY id")
        .bind("entity_type", entityType)
        .bind("entity_id", entityId)
        .map(LongMapper.FIRST)
        .first();
    return Optional.fromNullable(availableId);
  }

  private Map<String, Object> draftLinks(final long draftId,
                                         final Handle handle) {
    logger.debug("getting draft '{}' links", draftId);
    final List<Map<String, Object>> linkRecords = handle.createQuery(
        "SELECT draft_id, entity, entity_id FROM " + DRAFTS_LINKS_TABLE + " " +
            "WHERE draft_id=:draft_id")
        .bind("draft_id", draftId)
        .list();
    final Map<String, Object> links = Maps.newLinkedHashMap();
    for (Map<String, Object> record : linkRecords) {
      links.put(record.get("entity").toString(), record.get("entity_id"));
    }
    return links;
  }

  private String toJson(Object data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
