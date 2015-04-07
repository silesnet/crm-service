package net.snet.crm.service.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Longs;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.service.utils.Databases.lastEntityIdFor;

public class EntityIdFactory {
  private static final Logger logger =
      LoggerFactory.getLogger(EntityIdFactory.class);

  private static Map<String, Long> AGREEMENT_COUNTRY = ImmutableMap.of(
      "CZ", 100000L,
      "PL", 200000L
  );

  public static EntityId nextEntityIdFor(
      final String type,
      final String spate,
      final Handle handle) {
    checkNotNull(type, "entity type can't be null");
    checkNotNull(type, "entity spate can't be null");
    logger.debug("providing EntityId for '{}.{}'", type, spate);
    if ("agreements".equals(type)) {
      return new EntityId() {
        final long nextAgreementId = nextAgreementId(spate, type, handle);
        @Override
        public long nextId() {
          return nextAgreementId;
        }
      };
    }

    if ("services".equals(type)) {
      return new EntityId() {
        final long nextServiceId = nextServiceId(spate, type, handle);
        @Override
        public long nextId() {
          return nextServiceId;
        }
      };
    }

    return new EntityId() {
      final long nextEntityId = lastEntityIdFor(type, spate, handle) + 1;
      @Override
      public long nextId() {
        return nextEntityId;
     }
    };
  }

  private static long nextAgreementId(String spate, String type, Handle handle) {
    final String country = spate.toUpperCase();
    checkState(AGREEMENT_COUNTRY.containsKey(country),
        "unknown agreement country '%s'", country);
    long lastId = lastEntityIdFor(
        type, spate, "country='" + country + "'", handle);
    if (lastId == 0) {
      lastId = AGREEMENT_COUNTRY.get(country);
    }
    return lastId + 1;
  }

  private static long nextServiceId(String spate, String type, Handle handle) {
    final Long agreementId = Longs.tryParse(spate);
    checkNotNull(agreementId,
        "not numeric agreement id '%s'", spate);
    final String constrain = String.format("(id/100)=%d", agreementId);
    long lastId = lastEntityIdFor(type, spate, constrain, handle);
    if (lastId == 0) {
      lastId = agreementId * 100;
    }
    checkState((lastId % 100) < 99,
        "no free service id for agreement '%s', last service id found '%s'",
        agreementId, lastId);
    final long nextId = lastId + 1;
    logger.debug("nextId for '{}.{}' is '{}'", type, spate, nextId);
    return nextId;
  }
}
