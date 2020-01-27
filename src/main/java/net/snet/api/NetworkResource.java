package net.snet.api;

import com.google.common.collect.*;
import io.dropwizard.auth.Auth;
import net.snet.crm.service.auth.AuthenticatedUser;
import net.snet.network.NetworkComponent;
import net.snet.network.Node;
import net.snet.network.NodeQuery;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/api/networks")
@PermitAll
@Produces({"application/vnd.api+json"})
public class NetworkResource {
  private final NetworkComponent networkComponent;

  public NetworkResource(NetworkComponent networkComponent) {
    this.networkComponent = networkComponent;
  }

  @GET
  @Path("/nodes")
  public Response findNodes(@QueryParam("q") String query, @Auth AuthenticatedUser principal) {
    final NodeQuery nodeQuery = new NodeQuery(query);
    final Iterable<Node> nodes = networkComponent.findNodes(nodeQuery);
    return Response.ok().entity(
        ImmutableMap.of("data", mapIterable(nodes, this::toJsonApi))
    ).build();
  }

  private <T, R> Iterable<R> mapIterable(Iterable<T> iterable, Function<T, R> fn) {
    return Lists.newArrayList(iterable).stream()
        .map(fn)
        .collect(Collectors.toList());
  }

  private Map<String, Object> toJsonApi(Node node) {
    LinkedHashMap<Object, Object> attributes = Maps.newLinkedHashMap();
    attributes.put("name", node.getName());
    attributes.put("master", node.getMaster());
    attributes.put("area", node.getArea());
    attributes.put("vendor", node.getVendor());
    attributes.put("model", node.getModel());
    attributes.put("link-to", node.getLinkTo());
    attributes.put("rstp-num-ring", node.getRstpNumRing());
    attributes.put("backup-path", node.getBackupPath());
    return ImmutableMap.of(
        "type", "nodes",
        "id", node.getId(),
        "attributes", attributes
    );
  }
}
