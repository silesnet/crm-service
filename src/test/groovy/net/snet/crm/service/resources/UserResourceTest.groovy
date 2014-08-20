package net.snet.crm.service.resources

import com.sun.jersey.api.client.ClientResponse
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.dao.CrmRepository
import org.junit.ClassRule
import org.skife.jdbi.v2.DBI
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by admin on 20.8.14.
 */
class UserResourceTest extends Specification {
	private static CrmRepositoryDelegate CUSTOMER_REPO_DELEGATE = new CrmRepositoryDelegate()

	CrmRepository crmRepository

	@ClassRule
	@Shared
	ResourceTestRule resources = ResourceTestRule.builder()
			.addResource(new UserResource(Mock(DBI), CUSTOMER_REPO_DELEGATE))
			.build()

	def setup() {
		crmRepository = Mock(CrmRepository)
		CUSTOMER_REPO_DELEGATE.setRepository(crmRepository)
	}

	def cleanup() {
		CUSTOMER_REPO_DELEGATE.setRepository(null)
	}

	def 'it should fail to authenticate when no parameter given'() {
		given: 'user resource'
		when: 'requesting current user without parameter'
			def response = resources.client().resource('/users/current')
					.type('application/json').get(ClientResponse.class)
		then:
			response.status == 404
	}

	def 'it should authenticate test user via session parameter'() {
		given: 'user resource'
		when: 'requesting current user via test session parameter'
			def response = resources.client().resource('/users/current').queryParam('session', 'test')
				.type('application/json').get(ClientResponse.class)
		  def user = response.getEntity(Map.class).users
		then:
			response.status == 200
			user.user == 'test'
		  user.roles == 'ANONYMOUS_USER'
	}

	def 'it should authenticate test user via key parameter'() {
		given: 'user resource'
		when: 'requesting current user via test key parameter'
			def response = resources.client().resource('/users/current').queryParam('key', 'test')
				.type('application/json').get(ClientResponse.class)
		  def user = response.getEntity(Map.class).users
		then:
			response.status == 200
			user.user == 'test'
		  user.roles == 'ANONYMOUS_USER'
	}

	static class CrmRepositoryDelegate implements CrmRepository {
		@Delegate
		CrmRepository repository;

		def setRepository(CrmRepository repository) {
			this.repository = repository
		}
	}

}
