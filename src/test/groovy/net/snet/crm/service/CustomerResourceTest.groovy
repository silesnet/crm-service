package net.snet.crm.service

import com.sun.jersey.api.client.ClientResponse
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.dao.CrmRepository
import net.snet.crm.service.resources.CustomerResource
import org.junit.ClassRule
import org.skife.jdbi.v2.DBI
import spock.lang.Shared
import spock.lang.Specification

class CustomerResourceTest extends Specification {
  private static CrmRepositoryDelegate CUSTOMER_REPO_DELEGATE = new CrmRepositoryDelegate()

  @Shared
  DBI JDBI = Mock(DBI.class)

  CrmRepository crmRepository

  @ClassRule
  @Shared
  ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CustomerResource(JDBI, CUSTOMER_REPO_DELEGATE))
      .build()

  def setup() {
    crmRepository = Mock(CrmRepository)
    CUSTOMER_REPO_DELEGATE.setRepository(crmRepository)
  }

  def cleanup() {
    CUSTOMER_REPO_DELEGATE.setRepository(null)
  }

  def 'it should create new customer'() {
    given: 'new customer'
      def customer = [name: 'New Customer Name']
    when:
      ClientResponse response = resources.client().resource('/customers')
          .type('application/json')
          .post(ClientResponse.class, [customers: customer])
      def body = response.getEntity(Map.class)
    then:
      1 * crmRepository.insertCustomer(_) >> [id: '1234', name: 'New Customer Name']
      response.status == 201
      response.location.toString() ==~ /.*\/customers\/\d+$/
      response.type.toString().startsWith('application/json')
      body.customers.id == '1234'
      response.location.toString().endsWith(body.customers.id as String)
      body.customers.name == 'New Customer Name'
  }

  def 'it should create new agreement for given customer'() {
    given: 'existing customer'
    when: 'agreement created'
      def response = resources.client().resource('/customers/1234/agreements')
          .type('application/json').post(ClientResponse.class, [agreements: [country: 'CZ']])
      def agreement = response.getEntity(Map.class).agreements
    then:
      1 * crmRepository.findCustomerById(1234) >> [id: 1234L, name: 'Existing Customer']
    then:
      1 * crmRepository.insertAgreement(1234, 'CZ') >> [id: 10201L, customer_id: 1234L, country: 'CZ']
      response.status == 201
      response.location.toString() ==~ /.*\/agreements\/\d+$/
      response.type.toString().startsWith('application/json')
      agreement.id == 10201
      agreement.customer_id == 1234
      agreement.country == 'CZ'
  }

  static class CrmRepositoryDelegate implements CrmRepository {
    @Delegate
    CrmRepository repository;

    def setRepository(CrmRepository repository) {
      this.repository = repository
    }
  }

}