package net.snet.network.command;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;
import net.snet.network.NetworkRepository;
import net.snet.crm.service.auth.AuthenticatedUser;
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
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    final Map<String, Object> attributes = transform(resource.attributes());
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

  private Map<String, Object> transform(Map<String, Object> map) {

    final Map<String, Integer> nodeTypes = new HashMap<>();
    nodeTypes.put("OTHER", 10);
    nodeTypes.put("ROUTER", 20);
    nodeTypes.put("BRIDGE", 30);
    nodeTypes.put("BRIDGE-AP", 30);
    nodeTypes.put("BRIDGE-BR", 30);
    nodeTypes.put("BRIDGE-STATION", 30);
    nodeTypes.put("SWITCH", 40);
    nodeTypes.put("CONVERTER", 50);

    final Map<String, Integer> modeTypes = ImmutableMap.of(
        "BRIDGE-AP", 10,
        "BRIDGE-BR", 20,
        "BRIDGE-STATION", 30
    );

    // auth
    final Map<String, String> authentications = ImmutableMap.of(
        "NONE", "10",
        "BOTH", "20",
        "MAC ACL", "30",
        "RADIUS", "40"
    );

    final Map<String, Integer> countries = ImmutableMap.of(
        "CZ", 10,
        "PL", 20
    );
    map.put("linkto", map.get("linkTo"));
    map.remove("linkTo");

    final Map<String, Integer> polarizations = ImmutableMap.of(
      "HORIZONTAL", 10,
      "VERTICAL", 20,
      "DUAL", 30
    );
    final Function<String, String> identityFn = (String value) -> value;

    final ImmutableMap<String, Function<String, ? extends Serializable>> conversions = ImmutableMap.of(
        "type", nodeTypes::get,
        "mode", modeTypes::get,
        "auth", authentications::get,
        "country", countries::get,
        "polarization", polarizations::get
    );

    final ImmutableMap<String, String> mapping = ImmutableMap.of(
        "linkTo", "linkto",
        "authentication", "auth",
        "type", "type",
        "_type", "mode"
    );

    final HashMap<String, Object> updatedMap = new HashMap<>(map);
    if (map.containsKey("type")) {
      updatedMap.put("_type", map.get("type"));
    }

    return updatedMap.entrySet().stream().collect(Collectors.toMap(
      entry -> mapping.getOrDefault(entry.getKey(), entry.getKey()),
      entry -> conversions.getOrDefault(
          mapping.getOrDefault(entry.getKey(), entry.getKey()), identityFn).apply(
          Optional.ofNullable(entry.getValue()).orElse("").toString()
      )
    ));
  }
}
