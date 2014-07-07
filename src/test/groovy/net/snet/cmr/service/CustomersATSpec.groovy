package net.snet.cmr.service

import spock.lang.Specification

/**
 * Created by admin on 5.7.14.
 */
class CustomersATSpec extends Specification {
	def 'AT1-1 test'() {
		given:
			println 'AT1 test 1...'
		expect:
			true
	}

	def 'AT1-2 test'() {
		given:
			println 'AT1 test 2...'
		expect:
			true
	}
}
