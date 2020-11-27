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
import net.snet.network.shared.JsonApiResource;

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
  private final ObjectMapper mapper = new ObjectMapper();
  private final NetworkWriteRepository writeRepository;
  private final NetworkRepository readRepository;

  public NetworkCommandResource(NetworkWriteRepository writeRepository, NetworkRepository readRepository) {
    this.writeRepository = writeRepository;
    this.readRepository = readRepository;
    this.mapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);
  }

  @POST
  @Path("/nodes")
  public Response insertNode(JsonApiBody body, @Auth AuthenticatedUser user) {
    final JsonApiResource resource = body.resource();
    LOGGER.info("resource {}", resource);
    final Map<String, Object> attributes = mapNodeDetailToNode(resource.attributes());
    LOGGER.info("attributes {}", attributes);
    final Node node = new Node(attributes);
    final Node inserted = writeRepository.insertNode(node);
    final Object id = inserted.getAttributes().get("id");
    LOGGER.info("node id {}", id);
    final NodeId nodeId = new NodeId(id.toString());
    LOGGER.info("inserted node id {}", nodeId);
    final net.snet.network.Node model = readRepository.fetchNode(nodeId).get();
    LOGGER.info("model {}", model);
    final Map<String, Object> nodeAttributes = mapper.convertValue(model, new TypeReference<Map<String, Object>>() {});
    LOGGER.info("node attributes {}", nodeAttributes);
    return Response.created(URI.create("1")).entity(
        ImmutableMap.of("data", ImmutableMap.of(
            "id", model.getId(),
            "type", "nodes",
            "attributes", nodeAttributes))).build();
  }
}
