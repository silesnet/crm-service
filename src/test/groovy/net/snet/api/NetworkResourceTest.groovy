package net.snet.api

import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.network.NetworkComponent
import net.snet.network.Node
import net.snet.network.NodeFilter
import net.snet.network.NodeQuery
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

class NetworkResourceTest extends Specification {
  @Shared networkComponent = new NetworkComponent() {
    @Override
    Iterable<Node> findNodes(NodeQuery query) {
      return []
    }

    @Override
    Iterable<Node> findNodes(NodeFilter filter) {
      return []
    }
  }

  @ClassRule
  @Shared
  ResourceTestRule RULE = ResourceTestRule.builder()
    .addResource(new NetworkResource(networkComponent))
    .build()

  def 'should search by node filter'() {
    when:
    def response = RULE.target("/api/networks/nodes")
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
    def response = RULE.target("/api/networks/nodes")
        .queryParam("q", "node-name")
        .request().get(String)
    then:
    response == '{"data":[]}'
  }
}
