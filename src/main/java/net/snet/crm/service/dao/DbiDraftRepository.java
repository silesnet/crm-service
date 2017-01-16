package net.snet.crm.service.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.VoidHandleCallback;
import org.skife.jdbi.v2.util.LongMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.domain.model.draft.Draft.Entity.AGREEMENTS;
import static net.snet.crm.service.dao.EntityIdFactory.nextEntityIdFor;
import static net.snet.crm.service.utils.Databases.*;
import static net.snet.crm.service.utils.Entities.*;

public class DbiDraftRepository implements DraftRepository
{
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

  public DbiDraftRepository(final DBI dbi, final ObjectMapper objectMapper) {
    this.dbi = dbi;
    this.objectMapper = objectMapper;
  }

  @Override
  public long create(final Map<String, Object> draft) {
    logger.debug("creating draft");
    final Map<String, Object> record = recordOf(draft, DRAFT_FIELDS);
    final String entityType = valueOf("entity_type", record, String.class);
    final String entitySpate = valueOf("entity_spate", record, String.class);
    logger.debug("entity type.spate: {}.{}", entityType, entitySpate);
    if (record.containsKey("data")) {
      record.put("data", toJson(record.get("data")));
    }
    final Optional<Map<String, Object>> links = optionalMapOf("links", draft);
    return dbi.withHandle(new HandleCallback<Long>()
    {
      @Override
      public Long withHandle(Handle handle) throws Exception {
        final long draftId;
        final Optional<Long> availableDraftId =
            availableDraftIdByEntityTypeAndSpate(entityType, entitySpate, handle);
        if (availableDraftId.isPresent()) {
          draftId = availableDraftId.get();
          updateRecord(DRAFTS_TABLE, draftId, record, handle);
        } else {
          final EntityId entityId =
              nextEntityIdFor(entityType, entitySpate, handle);
          record.put("entity_id", entityId.nextId());
          draftId = insertRecord(DRAFTS_TABLE, record, handle);
        }
        if (links.isPresent()) {
          insertLinks(draftId, links.get(), handle);
        }
        return draftId;
      }
    });
  }

  @Override
  public void update(final long draftId, final Map<String, Object> update) {
    dbi.withHandle(new VoidHandleCallback()
    {
      @Override
      protected void execute(Handle handle) throws Exception {
        update(draftId, MapData.of(update), handle);
      }
    });
  }

  @Override
  public void update(long draftId, Data update, Handle handle) {
    final Map<String, Object> record = recordOf(update.asMap(), DRAFT_FIELDS);
    record.remove("id");
    record.remove("entity_type");
    record.remove("entity_id");
    if (record.containsKey("data")) {
      record.put("data", toJson(record.get("data")));
    }
    if (!record.isEmpty()) {
      updateRecord(DRAFTS_TABLE, draftId, record, handle);
    }
    final Map<String, Object> links = update.optionalMapOf("links");
    if (!links.isEmpty()) {
      deleteLinks(draftId, handle);
      insertLinks(draftId, links, handle);
    }
  }

  @Override
  public void delete(final long draftId) {
    logger.debug("deleting draft '{}'", draftId);
    final Draft draft = new Draft(get(draftId));
    dbi.inTransaction(new TransactionCallback<Void>()
    {
      @Override
      public Void inTransaction(Handle handle, TransactionStatus status) throws Exception {
        if (AGREEMENTS.equals(draft.entity())) {
          updateRecord(
              DRAFTS_TABLE, draftId, ImmutableMap.<String, Object>of("status", "AVAILABLE"), handle);
        } else {
          deleteDraft(draftId, handle);
        }
        deleteLinks(draftId, handle);
        return null;
      }
    });
  }

  @Override
  public Map<String, Object> get(final long draftId) {
    return dbi.withHandle(new HandleCallback<Map<String, Object>>()
    {
      @Override
      public Map<String, Object> withHandle(Handle handle) throws Exception {
        return getByDraftId(draftId, handle);
      }
    });
  }

  @Override
  public Data get(long draftId, Handle handle) {
    return MapData.of(getByDraftId(draftId, handle));
  }

  @Override
  public Map<String, Object> getEntity(
      final String entityType,
      final long entityId)
  {
    return dbi.withHandle(new HandleCallback<Map<String, Object>>()
    {
      @Override
      public Map<String, Object> withHandle(Handle handle) throws Exception {
        final Optional<Long> draftId = draftIdByEntityTypeAndId(entityType, entityId, handle);
        checkState(draftId.isPresent(),
                   "can't find draft by entity '%s/%s'", entityType, entityId);
        return getByDraftId(draftId.get(), handle);
      }
    });
  }

  @Override
  public List<Map<String, Object>> findByStatus(final String status) {
    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>()
    {
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
  public List<Map<String, Object>> findByOwnerAndStatus(
      final String owner,
      final String status)
  {

    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>()
    {
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

  private Map<String, Object> getByDraftId(
      final long draftId,
      final Handle handle)
  {
    final Map<String, Object> entity =
        entityOf(getRecord(DRAFTS_TABLE, draftId, handle), DRAFT_FIELDS);
    if (entity.containsKey("data")) {
      Optional<Map<String, Object>> map = toMap(String.valueOf(entity.get("data")));
      if (map.isPresent()) {
        entity.put("data", map.get());
      }
    }
    final Map<String, Object> links = draftLinks(draftId, handle);
    if (!links.isEmpty()) {
      entity.put("links", links);
    }
    return entity;
  }

  @Nonnull
  @SuppressWarnings("unchecked")
  private Optional<Map<String, Object>> toMap(@Nonnull String json) {
    try {
      final Map<String, Object> map = objectMapper.readValue(json, Map.class);
      return Optional.of(map);
    } catch (IOException e) {
      return Optional.absent();
    }
  }

  private Optional<Long> availableDraftIdByEntityTypeAndSpate(
      final String entityType,
      final String entitySpate,
      final Handle handle)
  {
    final Long availableId = handle.createQuery(
        "SELECT id FROM " + DRAFTS_TABLE +
            " WHERE 1=1" +
            " AND entity_type=:entity_type" +
            " AND entity_spate=:entity_spate" +
            " AND status='AVAILABLE'" +
            " ORDER BY entity_id" +
            " LIMIT 1")
                                   .bind("entity_type", entityType)
                                   .bind("entity_spate", entitySpate)
                                   .map(LongMapper.FIRST)
                                   .first();
    return Optional.fromNullable(availableId);
  }

  private Optional<Long> draftIdByEntityTypeAndId(
      final String entityType,
      final long entityId,
      final Handle handle)
  {
    final Long draftId = handle.createQuery(
        "SELECT id FROM " + DRAFTS_TABLE +
            " WHERE 1=1" +
            " AND entity_type=:entity_type" +
            " AND entity_id=:entity_id" +
            " ORDER BY id" +
            " LIMIT 1")
                               .bind("entity_type", entityType)
                               .bind("entity_id", entityId)
                               .map(LongMapper.FIRST)
                               .first();
    return Optional.fromNullable(draftId);
  }

  private Map<String, Object> draftLinks(
      final long draftId,
      final Handle handle)
  {
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

  private void insertLinks(long draftId, Map<String, Object> links, Handle handle) {
    for (Map.Entry<?, ?> link : links.entrySet()) {
      final Map<String, Object> linkRecord = ImmutableMap.<String, Object>builder()
          .put("draft_id", draftId)
          .put("entity", link.getKey())
          .put("entity_id", link.getValue()).build();
      insertRecordWithoutKey(DRAFTS_LINKS_TABLE, linkRecord, handle);
    }
  }

  private void deleteDraft(final long draftId, final Handle handle) {
    int deleted = handle
        .createStatement("DELETE FROM " + DRAFTS_TABLE + " WHERE id=:id")
        .bind("id", draftId)
        .execute();
    if (deleted == 0) {
      throw new WebApplicationException(new IllegalArgumentException(
          "can't delete draft '" + draftId + "'"),
                                        Response.Status.BAD_REQUEST);
    }
  }

  private void deleteLinks(long draftId, Handle handle) {
    handle.createStatement(
        "DELETE FROM " + DRAFTS_LINKS_TABLE + " WHERE draft_id=:draft_id")
          .bind("draft_id", draftId)
          .execute();
  }

  private String toJson(Object data) {
    try {
      return objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
