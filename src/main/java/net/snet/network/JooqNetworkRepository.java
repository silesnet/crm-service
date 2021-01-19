package net.snet.network;

import jersey.repackaged.com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import net.snet.crm.infra.db.query.tables.pojos.Nodes;
import net.snet.crm.infra.db.query.tables.pojos.NodesDetail;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.snet.crm.infra.db.query.tables.Nodes.NODES;
import static net.snet.crm.infra.db.query.tables.NodesDetail.NODES_DETAIL;

@Slf4j
public class JooqNetworkRepository implements NetworkRepository {

  private static final Function<Nodes, NodeItem> NODE_ITEM_MAPPER = (nodeItem) -> new NodeItem(
      nodeItem.getId(),
      nodeItem.getName(),
      nodeItem.getMaster(),
      nodeItem.getArea(),
      nodeItem.getVendor(),
      nodeItem.getModel(),
      nodeItem.getLinkTo(),
      nodeItem.getCountry(),
      nodeItem.getWidth(),
      nodeItem.getFrequency()
  );

  private static final Function<NodesDetail, Node> NODE_MAPPER = (node) -> new Node(
      node.getId(),
      node.getCountry(),
      node.getName(),
      node.getType(),
      node.getMaster(),
      node.getLinkTo(),
      node.getArea(),
      node.getVendor(),
      node.getModel(),
      node.getInfo(),
      node.getMonitoring(),
      node.getPath(),
      node.getPing(),
      node.getIsWireless(),
      node.getPolarization(),
      node.getWidth(),
      node.getNorm(),
      node.getTdma(),
      node.getAggregation(),
      node.getSsid(),
      node.getFrequency(),
      node.getPower(),
      node.getAntenna(),
      node.getWds(),
      node.getAuthentication(),
      node.getAzimuth(),
      node.getActive()
  );
  private static final ImmutableList<String> COUNTRY_OPTIONS = ImmutableList.of(
      "CZ",
      "PL"
  );
  private static final ImmutableList<String> TYPE_OPTIONS = ImmutableList.of(
      "OTHER",
      "ROUTER",
      "BRIDGE",
      "BRIDGE-AP",
      "BRIDGE-BR",
      "BRIDGE-STATION",
      "SWITCH"
  );
  private static final ImmutableList<String> MONITORING_TYPE_OPTIONS = ImmutableList.of(
      "NONE",
      "PING",
      "PING-SMS"
  );
  private static final ImmutableList<String> POLARIZATION_OPTIONS = ImmutableList.of(
      "HORIZONTAL",
      "VERTICAL",
      "DUAL"
  );
  private static final ImmutableList<String> AUTHENTICATION_OPTIONS = ImmutableList.of(
      "NONE",
      "BOTH",
      "MAC",
      "RADIUS"
  );

  private final DSLContext db;

  public JooqNetworkRepository(DSLContext dslContext) {
    this.db = dslContext;
  }

  @Override
  public Iterable<NodeItem> findNodes(NodeQuery query) {
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
        .map(NODE_ITEM_MAPPER)
        .collect(Collectors.toList());
  }

  @Override
  public Iterable<NodeItem> findNodes(NodeFilter filter) {
    LOGGER.debug("find node items by '{}", filter);
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
        .map(NODE_ITEM_MAPPER)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<Node> fetchNode(NodeId nodeId) {
    LOGGER.debug("fetch nodes by id '{}", nodeId);
    final Condition condition = nodeId.isName()
        ? NODES_DETAIL.NAME.eq(nodeId.getValue())
        : NODES_DETAIL.ID.eq(Integer.valueOf(nodeId.getValue()));
    return db.select().from(NODES_DETAIL)
        .where(condition)
        .fetchOptionalInto(NodesDetail.class)
        .map(NODE_MAPPER);
  }

  @Override
  public Map<String, Iterable<String>> fetchNodeOptions() {
    final Map<String, Iterable<String>> options = new HashMap<>();
    options.put("countries", COUNTRY_OPTIONS);
    options.put("types", TYPE_OPTIONS);
    options.put("masters", fetchOptions(NODES_DETAIL.MASTER));
    // FIXME: links should be removed
    options.put("links", fetchOptions(NODES_DETAIL.LINK_TO));
    options.put("areas", fetchOptions(NODES_DETAIL.AREA));
    options.put("vendors", fetchOptions(NODES_DETAIL.VENDOR));
    options.put("models", fetchOptions(NODES_DETAIL.MODEL));
    options.put("monitoring-types", MONITORING_TYPE_OPTIONS);
    options.put("polarizations", POLARIZATION_OPTIONS);
    options.put("channel-widths", fetchOptions(NODES_DETAIL.WIDTH));
    options.put("norms", fetchOptions(NODES_DETAIL.NORM));
    options.put("frequencies", fetchOptions(NODES_DETAIL.FREQUENCY.cast(String.class)));
    options.put("authentications", AUTHENTICATION_OPTIONS);
    return options;
  }

  private List<String> fetchOptions(Field field) {
    return db.selectDistinct(field).from(NODES_DETAIL).where(field.isNotNull()).orderBy(field).fetch(field);
  }

  private Optional<Condition> condition(final Field<?> field, final String prefix) {
    if (prefix == null || prefix.trim().isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(field.likeIgnoreCase(prefix.replaceAll("\\*", "%")));
  }

}
