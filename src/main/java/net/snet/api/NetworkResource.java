package net.snet.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;
import net.snet.crm.service.auth.AuthenticatedUser;
import net.snet.network.*;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Path("/api/networks")
@PermitAll
@Produces({"application/vnd.api+json"})
public class NetworkResource {
  private final NetworkComponent networkComponent;

  public NetworkResource(NetworkComponent networkComponent) {
    this.networkComponent = networkComponent;
  }

  @GET
  @Path("/node-items")
  public Response findNodeItems(
      @QueryParam("name") Optional<String> name,
      @QueryParam("master") Optional<String> master,
      @QueryParam("area") Optional<String> area,
      @QueryParam("linkTo") Optional<String> linkTo,
      @QueryParam("vendor") Optional<String> vendor,
      @QueryParam("country") Optional<String> country,
      @QueryParam("q") Optional<String> query,
      @Auth AuthenticatedUser principal) {
    final NodeFilter.NodeFilterBuilder builder = NodeFilter.builder();
    name.ifPresent(builder::name);
    master.ifPresent(builder::master);
    area.ifPresent(builder::area);
    linkTo.ifPresent(builder::linkTo);
    vendor.ifPresent(builder::vendor);
    country.ifPresent(builder::country);
    final NodeFilter nodeFilter = builder.build();
    final Iterable<NodeItem> nodes = query.isPresent()
        ? networkComponent.findNodes(new NodeQuery(query.get()))
        : networkComponent.findNodes(nodeFilter);
    return Response.ok().entity(
        ImmutableMap.of("data", mapIterable(nodes, this::toJsonApi))
    ).build();
  }

  @GET
  @Path("/nodes/{nodeId}")
  public Response fetchNode(@PathParam("nodeId") NodeId nodeId) {
    final Optional<Node> node = networkComponent.fetchNode(nodeId);
    return Response.ok().entity(node.isPresent() ? ImmutableMap.of("data", toJsonApi(node.get())) : notFound()).build();
  }

  @GET
  @Path("nodes/options")
  public Response fetchNodeLabels() {
    return Response.ok()
        .entity(ImmutableMap.of("data",
            ImmutableMap.of(
                "type", "options",
                "id", "nodes.options",
                "attributes", networkComponent.fetchNodeOptions()
            ))
        ).build();
  }

  private ImmutableMap<String, ArrayList<ImmutableMap<String, Integer>>> notFound() {
    return ImmutableMap.of("errors", Lists.newArrayList(ImmutableMap.of("status", 404)));
  }

  private <T, R> Iterable<R> mapIterable(Iterable<T> iterable, Function<T, R> fn) {
    return Lists.newArrayList(iterable).stream()
        .map(fn)
        .collect(Collectors.toList());
  }

  private Map<String, Object> toJsonApi(NodeItem nodeItem) {
    LinkedHashMap<Object, Object> attributes = Maps.newLinkedHashMap();
    attributes.put("name", nodeItem.getName());
    attributes.put("master", nodeItem.getMaster());
    attributes.put("area", nodeItem.getArea());
    attributes.put("vendor", nodeItem.getVendor());
    attributes.put("model", nodeItem.getModel());
    attributes.put("link-to", nodeItem.getLinkTo());
    attributes.put("country", nodeItem.getCountry());
    attributes.put("frequency", nodeItem.getFrequency());
    return ImmutableMap.of(
        "type", "node-items",
        "id", nodeItem.getId(),
        "attributes", attributes
    );
  }

  private Map<String, Object> toJsonApi(Node node) {
    LinkedHashMap<Object, Object> attributes = Maps.newLinkedHashMap();
    attributes.put("country", node.getCountry());
    attributes.put("name", node.getName());
    attributes.put("type", node.getType());
    attributes.put("master", node.getMaster());
    attributes.put("link-to", node.getLinkTo());
    attributes.put("area", node.getArea());
    attributes.put("vendor", node.getVendor());
    attributes.put("model", node.getModel());
    attributes.put("info", node.getInfo());
    attributes.put("monitoring", node.getMonitoring());
    attributes.put("path", node.getPath());
    attributes.put("ping", node.getPing());
    attributes.put("is-wireless", node.isWireless());
    attributes.put("polarization", node.getPolarization());
    attributes.put("width", node.getWidth());
    attributes.put("norm", node.getNorm());
    attributes.put("tdma", node.getTdma());
    attributes.put("aggregation", node.getAggregation());
    attributes.put("ssid", node.getSsid());
    attributes.put("frequency", node.getFrequency());
    attributes.put("power", node.getPower());
    attributes.put("antenna", node.getAntenna());
    attributes.put("wds", node.getWds());
    attributes.put("authentication", node.getAuthentication());
    attributes.put("azimuth", node.getAzimuth());
    attributes.put("active", node.getActive());
    return ImmutableMap.of(
        "type", "nodes",
        "id", node.getId(),
        "attributes", attributes
    );
  }
}
