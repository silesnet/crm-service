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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/api/networks")
@PermitAll
@Produces({"application/json; charset=UTF-8"})
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
        Lists.newArrayList(nodes).stream()
            .map(this::toJsonApi)
            .collect(Collectors.toList())
    ).build();
  }

  private Map<String, Object> toJsonApi(Node node) {
    LinkedHashMap<Object, Object> attributes = Maps.newLinkedHashMap();
    attributes.put("name", node.getName());
    attributes.put("master", node.getMaster());
    attributes.put("area", node.getArea());
    attributes.put("vendor", node.getVendor());
    attributes.put("model", node.getModel());
    attributes.put("linkTo", node.getLinkTo());
    attributes.put("rstpNumRing", node.getRstpNumRing());
    attributes.put("backupPath", node.getBackupPath());
    return ImmutableMap.of(
        "data", ImmutableMap.of(
            "type", "nodes",
            "id", node.getId(),
            "attributes", attributes
        )
    );
  }
}
