package net.snet.crm.service.resources

import com.sun.jersey.api.client.ClientResponse
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.dao.CrmRepository
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification


class ServiceResourceTest extends Specification {
  private static CrmRepositoryDelegate CUSTOMER_REPO_DELEGATE = new CrmRepositoryDelegate()

  CrmRepository crmRepository

  @ClassRule
  @Shared
  ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new ServiceResource(CUSTOMER_REPO_DELEGATE, null))
      .build()

  def setup() {
    crmRepository = Mock(CrmRepository)
    CUSTOMER_REPO_DELEGATE.setRepository(crmRepository)
  }

  def cleanup() {
    CUSTOMER_REPO_DELEGATE.setRepository(null)
  }

  def 'it should find services by query'() {
    given:
      def query = 'cus'
      def country = 'cz'
    when:
      def response = resources.client().resource('/services')
            .queryParam('q', query)
            .queryParam('country', 'cz')
            .type('application/json')
            .get(ClientResponse.class)
      def services = response.getEntity(Map.class).services
    then:
      1 * crmRepository.findService(query, country, true) >> [[id: 1, name: 'LAN1'], [id: 2, name: 'LAN2']]
    and:
      response.status == 200
      response.type.toString().startsWith('application/json')
    and:
      services.size() == 2
    and:
      services[0].id == 1
      services[0].name == 'LAN1'
      services[1].id == 2
      services[1].name == 'LAN2'
  }

  def 'it should create new connection for service'() {
    given: 'new connection'
      def connectionData = [auth_type: 'PPPoE', auth_name: 'user', auth_value: 'password']
    when: 'inserting new connection'
      def response = resources.client().resource('/services/100123401/connections').type('application/json')
          .post(ClientResponse.class, [connections: connectionData])
      def connection = response.getEntity(Map.class).connections
    then:
      1 * crmRepository.findServiceById(100123401) >> [id: 100123401]
      1 * crmRepository.insertConnection(100123401) >> [service_id: 100123401]
      1 * crmRepository.updateConnection(100123401, _) >> [service_id: 100123401, auth_type: 'PPPoE', auth_name: 'user', auth_value: 'password']
    and: 'response has correct headers'
      response.status == 201
      response.location.toString() ==~ /.*\/connections\/\d+$/
      response.type.toString().startsWith('application/json')
    and: 'connection is returned'
      connection.service_id == 100123401
      connection.auth_type == 'PPPoE'
      connection.auth_name == 'user'
      connection.auth_value == 'password'
  }

  static class CrmRepositoryDelegate implements CrmRepository {
    @Delegate
    CrmRepository repository;

    def setRepository(CrmRepository repository) {
      this.repository = repository
    }
  }
}
