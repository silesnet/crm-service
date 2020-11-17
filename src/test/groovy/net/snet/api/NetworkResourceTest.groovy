package net.snet.api

import com.google.common.collect.ImmutableMap
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.network.NetworkComponent
import net.snet.network.Node
import net.snet.network.NodeFilter
import net.snet.network.NodeId
import net.snet.network.NodeItem
import net.snet.network.NodeQuery
import org.assertj.core.util.Maps
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

class NetworkResourceTest extends Specification {
  @Shared networkComponent = new NetworkComponent() {
    @Override
    Iterable<NodeItem> findNodes(NodeQuery query) {
      return []
    }

    @Override
    Iterable<NodeItem> findNodes(NodeFilter filter) {
      return []
    }

    @Override
    Optional<Node> fetchNode(NodeId nodeId) {
      return Optional.empty()
    }

    @Override
    Map<String, Iterable<String>> fetchNodeOptions() {
      return ImmutableMap.of();
    }
  }

  @ClassRule
  @Shared
  ResourceTestRule RULE = ResourceTestRule.builder()
    .addResource(new NetworkResource(networkComponent))
    .build()

  def 'should search by node filter'() {
    when:
    def response = RULE.target("/api/networks/node-items")
        .queryParam("name", "node-name")
        .queryParam("master", "master-name")
        .queryParam("area", "area-name")
        .queryParam("linkTo", "linTo-name")
        .queryParam("vendor", "vendor-name")
        .queryParam("country", "country-name")
        .request().get(String)
    then:
      response == '{"data":[]}'
  }

  def 'should search by query'() {
    when:
    def response = RULE.target("/api/networks/node-items")
        .queryParam("q", "node-name")
        .request().get(String)
    then:
    response == '{"data":[]}'
  }

  def 'should return node detail'() {
    when:
    def response = RULE.target("/api/networks/nodes/node-id")
        .request().get(String)
    then:
    response == '{"errors":[{"status":404}]}'
  }
}
