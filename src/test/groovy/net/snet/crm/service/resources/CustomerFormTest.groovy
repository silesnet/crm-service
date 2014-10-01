package net.snet.crm.service.resources

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

/**
 * Created by admin on 30.9.14.
 */
class CustomerFormTest extends Specification {
	@Shared ObjectMapper om = new ObjectMapper();

	@Subject CustomerForm customerForm = new CustomerForm(customerFormDataSample())

	def 'it should map draft customer form data into customer table update'() {
		given: 'draft customer form data'
		when:
			def customerUpdate = customerForm.customerUpdate()
		then:
			println customerUpdate
			customerUpdate.name == 'Milan Kolouch'
	}

	Map<String, Object> customerFormDataSample() {
		om.readValue(Resources.getResource('draft-data-sample.json').text, Map.class).customer
	}
}
