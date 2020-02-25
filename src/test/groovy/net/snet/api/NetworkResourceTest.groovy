package net.snet.api

import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.network.NetworkComponent
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

class NetworkResourceTest extends Specification {
  @Shared networkComponent = Mock(NetworkComponent)

  @ClassRule
  @Shared
  ResourceTestRule RULE = ResourceTestRule.builder()
    .setTestContainerFactory(new GrizzlyTestContainerFactory())
    .addResource(new NetworkResource(networkComponent))
    .build()

  def 'should create nodes filter'() {
    def response = RULE.target("/api/networks/nodes2").request().get()

    println response
    expect:
      response != null
  }
}
