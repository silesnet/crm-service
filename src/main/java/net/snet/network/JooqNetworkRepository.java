package net.snet.network;

import lombok.extern.slf4j.Slf4j;
import net.snet.crm.infra.db.query.tables.pojos.Nodes;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.snet.crm.infra.db.query.tables.Nodes.NODES;

@Slf4j
class JooqNetworkRepository implements NetworkRepository {

  private static final Function<Nodes, Node> MAPPER = (node) -> new Node(
      node.getId(),
      node.getName(),
      node.getMaster(),
      node.getArea(),
      node.getVendor(),
      node.getModel(),
      node.getLinkTo(),
      node.getCountry(),
      node.getFrequency()
  );

  private final DSLContext db;

  public JooqNetworkRepository(DSLContext dslContext) {
    this.db = dslContext;
  }

  @Override
  public Iterable<Node> findNodes(NodeQuery query) {
    LOGGER.debug("find nodes by '{}'", query);
    return db.select().from(NODES)
        .where(DSL.condition("to_tsvector('english', {0}) @@ to_tsquery('english', {1})",
            DSL.concat(NODES.NAME,
                DSL.val(" "), DSL.coalesce(NODES.MASTER, ""),
                DSL.val(" "), DSL.coalesce(NODES.VENDOR, ""),
                DSL.val(" "), DSL.coalesce(NODES.LINK_TO, ""),
                DSL.val(" "), DSL.coalesce(NODES.AREA, "")),
            DSL.val(query.getValue() + ":*")))
        .fetchInto(Nodes.class)
        .stream()
        .map(MAPPER)
        .collect(Collectors.toList());
  }

  @Override
  public Iterable<Node> findNodes(NodeFilter filter) {
    LOGGER.debug("find nodes by '{}", filter);
    final Condition condition = Stream.of(
        condition(NODES.NAME, filter.getName()),
        condition(NODES.MASTER, filter.getMaster()),
        condition(NODES.AREA, filter.getArea()),
        condition(NODES.LINK_TO, filter.getLinkTo()),
        condition(NODES.VENDOR, filter.getVendor()),
        condition(NODES.COUNTRY, filter.getCountry())
    )
        .filter(Optional::isPresent)
        .map(Optional::get)
        .reduce(Condition::and)
        .orElse(DSL.condition(false));
    return db.select().from(NODES)
        .where(condition)
        .fetchInto(Nodes.class)
        .stream()
        .map(MAPPER)
        .collect(Collectors.toList());
  }

  private Optional<Condition> condition(final Field<?> field, final String prefix) {
    if (prefix == null || prefix.trim().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(field.likeIgnoreCase(prefix.replaceAll("\\*", "%")));
  }

}
