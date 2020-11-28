package net.snet.network.command

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.logging.BootstrapLogging
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.network.NetworkRepository
import net.snet.network.command.domain.model.NetworkWriteRepository
import net.snet.network.shared.JsonApiMessageBodyReader
import net.snet.network.shared.ResourceTestHelper
import org.junit.ClassRule
import org.spockframework.mock.MockUtil
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import javax.ws.rs.client.Entity

class NetworkCommandResourceTest extends Specification {
  private final static mockFactory = new DetachedMockFactory()
  private final static mockUtil = new MockUtil()

  private final static writeRepository = mockFactory.Mock(NetworkWriteRepository)
  private final static readRepository = mockFactory.Mock(NetworkRepository)

  @Shared
  def resource = new NetworkCommandResource(writeRepository, readRepository)

  @Shared
  @ClassRule
  ResourceTestRule testRule = ResourceTestHelper.resourceTestRuleBuilder()
      .addProvider(new JsonApiMessageBodyReader(new ObjectMapper()))
      .addResource(resource)
      .build()

  static {
    BootstrapLogging.bootstrap(Level.INFO)
  }

  void setup() {
    mockUtil.attachMock(writeRepository, this)
    mockUtil.attachMock(readRepository, this)
  }

  void cleanup() {
    mockUtil.detachMock(writeRepository)
    mockUtil.detachMock(readRepository)
  }

  def 'it should insert network node'() {
    when:
    def response = testRule.client().target('/api/networks/nodes')
        .request()
        .header('Authorization', 'Bearer TEST')
        .post(Entity.entity([data: [type: 'nodes', attributes: [
            country: 'CZ',
            type: 'BRIDGE-AP',
            name: 'node-1',
            'link-to': 'node-2',
            master: 'node-3',
            authentication: 'BOTH',
            polarization: 'VERTICAL',
            area: null
        ]]], 'application/vnd.api+json'), Map.class)
    then:
    1 * writeRepository.insertNode(!null) >> new net.snet.network.command.domain.model.Node([id: '1', name: 'node-1', linkTo: 'node-2'])
    1 * readRepository.fetchNode(!null) >> Optional.of(new net.snet.network.Node(1, null, 'node-1', null, null, 'node-2', null, null, null, null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, null, null))
    response.data.id == 1
    response.data.type == 'nodes'
    response.data.attributes.name == 'node-1'
    response.data.attributes['link-to'] == 'node-2'
  }
}
