package net.snet.crm.service

import com.sun.jersey.api.client.ClientResponse
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.dao.CustomerRepository
import net.snet.crm.service.resources.CustomerResource
import org.junit.ClassRule
import org.skife.jdbi.v2.DBI
import spock.lang.Shared
import spock.lang.Specification

class CustomerResourceTest extends Specification {
  private static CustomerRepositoryDelegate delegate = new CustomerRepositoryDelegate()

  @Shared DBI JDBI = Mock(DBI.class)

  def repository

  @ClassRule
  @Shared
  ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CustomerResource(JDBI, delegate))
      .build()

  def setup() {
    repository = Mock(CustomerRepository)
    delegate.setRepository(repository)
  }

  def cleanup() {
    delegate.setRepository(null)
  }

  def 'it should run'() {
    given: 'new customer'
      def customer = [ name: 'New Customer Name']
    when:
      ClientResponse response = resources.client().resource('/customers')
          .type('application/vnd.api+json')
          .post(ClientResponse.class, [ customers: customer ])
      def body = response.getEntity(Map.class)
    then:
      1 * repository.insert(_) >> [id: '1234', name: 'New Customer Name']
      response.status == 201
      response.location.toString() ==~ /^\/customers\/\d+$/
      response.type.toString().startsWith('application/vnd.api+json')
      body.customers.id == '1234'
      response.location.toString().endsWith(body.customers.id as String)
      body.customers.name == 'New Customer Name'
  }

  static class CustomerRepositoryDelegate implements CustomerRepository {
    @Delegate
    CustomerRepository repository;

    def setRepository(CustomerRepository repository) {
      this.repository = repository
    }

  }

}
