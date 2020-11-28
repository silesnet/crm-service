package net.snet.network.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;
import net.snet.crm.service.auth.AuthenticatedUser;
import net.snet.network.NetworkRepository;
import net.snet.network.NodeId;
import net.snet.network.command.domain.model.NetworkWriteRepository;
import net.snet.network.command.domain.model.Node;
import net.snet.network.shared.JsonApiBody;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static net.snet.network.shared.NodeMapping.mapNodeDetailToNode;

@Path("/api/networks")
@PermitAll
@Consumes({"application/vnd.api+json"})
@Produces({"application/vnd.api+json"})
@Slf4j
public class NetworkCommandResource {
  private final ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
  private final NetworkWriteRepository writeRepository;
  private final NetworkRepository readRepository;

  public NetworkCommandResource(NetworkWriteRepository writeRepository, NetworkRepository readRepository) {
    this.writeRepository = writeRepository;
    this.readRepository = readRepository;
  }

  @POST
  @Path("/nodes")
  public Response insertNode(JsonApiBody body, @Auth AuthenticatedUser user) {
    final Node node = new Node(mapNodeDetailToNode(body.resource().attributes()));
    final Node inserted = writeRepository.insertNode(node);

    final NodeId nodeId = new NodeId(inserted.getAttributes().get("id").toString());
    final net.snet.network.Node model = readRepository.fetchNode(nodeId)
        .orElseThrow(() -> new IllegalStateException("inserted node not found, node id: '" + nodeId.getValue() + "'"));

    final Map<String, Object> nodeAttributes = mapper.convertValue(model, new TypeReference<Map<String, Object>>() {});
    return Response.created(URI.create("" + model.getId())).entity(
        ImmutableMap.of("data", ImmutableMap.of(
            "id", model.getId(),
            "type", "nodes",
            "attributes", nodeAttributes))).build();
  }
}
