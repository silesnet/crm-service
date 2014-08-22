package net.snet.crm.service.resources

import spock.lang.Specification

import javax.ws.rs.core.UriBuilder

/**
 * Created by admin on 22.8.14.
 */
class DefaultUserServiceTest extends Specification {
	def 'it should compose uri'() {
	given:
		def serviceUri = new URI('https://localhost:8443/get_user')
	when:
		def targetUri = UriBuilder.fromUri(serviceUri).matrixParam('jsessionid', 'test').build()
	then:
		targetUri.toString() == 'https://localhost:8443/get_user;jsessionid=test'
	}
}
