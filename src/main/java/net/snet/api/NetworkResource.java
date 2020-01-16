package net.snet.api;

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
    return Response.ok().entity(nodes).build();
  }
}
