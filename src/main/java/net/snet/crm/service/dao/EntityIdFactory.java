package net.snet.crm.service.dao;

import net.snet.crm.service.utils.Databases;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.LongMapper;

public class EntityIdFactory {
  private static String DRAFTS_TABLE = "drafts2";

  public static EntityId entityIdFor(final String entityTypeAndClass,
                                     final Handle handle) {
    final int classIdx = entityTypeAndClass.lastIndexOf(".");
    final String entityClass = classIdx > 0 ?
                    entityTypeAndClass.substring(classIdx + 1) : "";
    final String entityType = classIdx > 0 ?
                    entityTypeAndClass.substring(0, classIdx) : entityTypeAndClass;
    if ("agreements".equals(entityType)) {
      return new EntityId() {
        @Override
        public long nextEntityId() {
          final long lastId = handle.createQuery("SELECT max(entity_id) FROM " +
              DRAFTS_TABLE + " WHERE " +
              "entity_type='agreements' AND entity_name=:entity_class;")
              .bind("entity_class", entityClass)
              .map(LongMapper.FIRST)
              .first();
          return lastId + 1;
        }
      };
    }
    if ("services".equals(entityType)) {
      return new EntityId() {
        @Override
        public long nextEntityId() {
          long lastId = handle.createQuery("SELECT max(entity_id) FROM " +
              DRAFTS_TABLE + " WHERE " +
              "entity_type='services' AND entity_name=:entity_class;")
              .bind("entity_class", entityClass)
              .map(LongMapper.FIRST)
              .first();
          if (lastId == 0) {
            lastId = Long.valueOf(entityClass) * 100;
          }
          return lastId + 1;
        }
      };
    }
    return new EntityId() {
      @Override
      public long nextEntityId() {
        return Databases.nextEntityIdFor(entityType, handle);
     }
    };
  }
}
