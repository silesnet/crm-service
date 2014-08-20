package net.snet.crm.service

import groovyx.net.http.RESTClient
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by admin on 5.7.14.
 */
class ServicesATSpec extends Specification {
	@Ignore
	def 'AT2-1 test'() {
		given:
			println 'AT2 test 1...'
			RESTClient client = new RESTClient('http://localhost:8080/')
			println client.get(path: 'base').data.timestamp
		  println client.get(path: 'customers', query: [q: 'mar']).data
		expect:
			true
	}

	@Ignore
	def 'AT2-2 test'() {
		given:
			println 'AT2 test 2...'
		expect:
			true
	}
}
