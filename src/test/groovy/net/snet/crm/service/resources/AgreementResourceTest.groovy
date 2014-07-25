package net.snet.crm.service.resources

import com.sun.jersey.api.client.ClientResponse
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.dao.CrmRepository
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

class AgreementResourceTest extends Specification {
  private static CrmRepositoryDelegate CUSTOMER_REPO_DELEGATE = new CrmRepositoryDelegate()

  CrmRepository crmRepository

  @ClassRule
  @Shared
  ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new AgreementResource(CUSTOMER_REPO_DELEGATE))
      .build()

  def setup() {
    crmRepository = Mock(CrmRepository)
    CUSTOMER_REPO_DELEGATE.setRepository(crmRepository)
  }

  def cleanup() {
    CUSTOMER_REPO_DELEGATE.setRepository(null)
  }

  def 'it should fetch agreement by id'() {
    given: 'agreement exists'
    when:
      def response = resources.client().resource('/agreements/1001234')
                       .type('application/json').get(ClientResponse.class)
      def agreement = response.getEntity(Map.class).agreements
    then:
      1 * crmRepository.findAgreementById(1001234) >> [agreements: [id: 1001234, country: 'CZ', customer_id: 1]]
    and: 'agreement is returned'
      agreement.id == 1001234
      agreement.country == 'CZ'
      agreement.customer_id == 1
  }

  def 'it should create new service for the agreement'() {
    given: 'existing agreement'
    when: 'inserting new service'
      def response = resources.client().resource('/agreements/1001234/services').type('application/json')
                      .post(ClientResponse.class)
      def service = response.getEntity(Map.class).services
    then:
      1 * crmRepository.findAgreementById(1001234) >> [id: 1001234, country: 'CZ', customer_id: 1]
      1 * crmRepository.insertService(1001234) >> [id: 100123401, status: 'NEW']
    and: 'response has correct headers'
      response.status == 201
      response.location.toString() ==~ /.*\/services\/\d+$/
      response.type.toString().startsWith('application/json')
    and: 'service is returned'
      service.id == 100123401
      service.status == 'NEW'
  }

  static class CrmRepositoryDelegate implements CrmRepository {
    @Delegate
    CrmRepository repository;

    def setRepository(CrmRepository repository) {
      this.repository = repository
    }
  }
}
