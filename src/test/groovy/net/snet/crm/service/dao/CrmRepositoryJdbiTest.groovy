package net.snet.crm.service.dao

import com.google.common.io.Resources
import org.joda.time.DateTime
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

class CrmRepositoryJdbiTest extends Specification {
  @Shared DBI dbi = new DBI("jdbc:h2:mem:customerRepositoryTest")
  @Shared Handle handle

  def setup() {
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
  }

  def cleanup() {
    handle.execute('DROP TABLE customers')
    handle.execute('DROP TABLE agreements')
    handle.execute('DROP TABLE services_info')
    handle.execute('DROP TABLE services')
    handle.execute('DROP TABLE connections')
    handle.close()
  }

  def 'it should insert new customer'() {
    given: 'new customer name'
      def customerName = 'New Name'
    and: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    and: 'customers table is empty'
      assert handle.select('SELECT count(*) as cnt from customers').first().cnt == 0
    when: 'new customers is inserted into repository'
      def insertedCustomer = repository.insertCustomer([name: customerName])
    then: 'inserted customer is populated from database'
      insertedCustomer.id == 1
      insertedCustomer.history_id == 1
      insertedCustomer.name == customerName
      insertedCustomer.public_id == '9999999'
      insertedCustomer.is_active == false
      insertedCustomer.customer_status == 'DRAFT'
      new DateTime().isAfter(insertedCustomer.inserted_on.getTime())
    and: 'customers table contains new row'
      handle.select('SELECT count(*) AS cnt FROM customers').first().cnt == 1
  }

	def 'it should delete customer'() {
		given: 'repository'
			def repository = new CrmRepositoryJdbi(dbi)
		and: 'existing customer'
			def customer = repository.insertCustomer([name: 'test'])
		  assert customer != null
		when: 'customer is deleted'
			repository.deleteCustomer(customer.id as Long)
		then: 'customer does not exist in the table'
			handle.select('SELECT count(*) AS cnt FROM customers WHERE id=' + customer.id).first().cnt == 0
	}

  def 'it should roll back on error inserting customer'() {
    given: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    when: 'invalid customer is inserted'
      repository.insertCustomer([name: null])
    then:
      thrown RuntimeException
    and:
      handle.select('SELECT count(*) as cnt FROM customers').first().cnt == 0
  }

  def 'it should insert new customer agreement'() {
    given: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    and: 'existing customer'
      def customer = repository.insertCustomer([name: 'existing customer'])
    and: 'country'
      def country = 'CZ'
    when: 'insert customer agreement for country'
      def agreement = repository.insertAgreement(customer.id as Long, country)
    then: 'agreement is inserted'
      agreement.id == 100001
      agreement.country == 'CZ'
      agreement.customer_id == 1
	    agreement.status == 'DRAFT'
	  and: 'agreements table contains new row'
		  handle.select('SELECT count(*) AS cnt FROM agreements').first().cnt == 1
    and: 'customer agreements are updated'
//      repository.findCustomerById(1).contract_no == '1'
  }

  def 'it should properly calculate agreement id'() {
    given: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    and: 'existing customer'
      def customer = repository.insertCustomer([name: 'existing customer'])
    and: 'country'
      def country = 'CZ'
    when: 'insert customer agreement for country twice'
      def agreement1 = repository.insertAgreement(customer.id as Long, country)
      def agreement2 = repository.insertAgreement(customer.id as Long, country)
    then: 'agreement is inserted'
      agreement1.id == 100001
      agreement2.id == 100002
    and: 'customer agreements are updated'
//      repository.findCustomerById(1).contract_no == '1, 2'
  }

	def 'it should update customer agreement status'() {
		expect:
			false
	}

  def 'it should insert new agreement service'() {
    given: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    and: 'existing customer and agreement'
      def customer = repository.insertCustomer([name: 'existing customer'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
    when: 'insert agreement service'
      def service = repository.insertService(agreement.id as Long)
      println service
    then: 'service is inserted'
      service.id == (agreement.id * 100) + 1
      service.customer_id == customer.id
      service.status == 'DRAFT'
  }

  def 'it should fail inserting 100th service for agreement'() {
    given: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    and: 'existing customer and agreement with 99 services'
      def customer = repository.insertCustomer([name: 'existing customer'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
      def lastService = [:]
      for (int i = 0; i < 99; i++) {
        lastService = repository.insertService(agreement.id as Long)
      }
    when: 'inserting 100th agreement service'
      repository.insertService(agreement.id as Long)
    then: 'service last service sequence is 99'
      lastService.id == (agreement.id * 100) + 99
    and: 'failed to insert 100th service'
      thrown RuntimeException
  }

	def 'it should delete service'() {
		expect:
			false
	}

  def 'it should insert new service connection'() {
    given: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    and: 'existing customer, agreement and service'
      def customer = repository.insertCustomer([name: 'existing customer'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
      def service = repository.insertService(agreement.id as Long)
    when: 'insert service connection'
      def connection = repository.insertConnection(service.id as Long)
    then: 'connection is inserted'
      connection.service_id == service.id
  }

	def 'it should delete service connection'() {
		expect:
			false
	}

  def 'it should update service connection'() {
    given: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    and: 'existing customer, agreement, service and connection'
      def customer = repository.insertCustomer([name: 'existing customer'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
      def service = repository.insertService(agreement.id as Long)
      def connection = repository.insertConnection(service.id as Long)
    and: 'connection fields to update'
      def update = [ auth_type : 'PPPoE', auth_name: 'joe', auth_value: 'password',
                     downlink: 100, uplink: 50, is_public_ip: true, ip: '10.0.0.1' ]
    when: 'updated service connection'
      def updatedConnection = repository.updateConnection(service.id as Long, update.entrySet())
    then: 'service connection is updated'
      connection.service_id == updatedConnection.service_id
      updatedConnection.auth_type == update.auth_type
      updatedConnection.auth_name == update.auth_name
      updatedConnection.auth_value == update.auth_value
      updatedConnection.downlink == update.downlink
      updatedConnection.uplink == update.uplink
      updatedConnection.is_public_ip == update.is_public_ip
      updatedConnection.ip == update.ip
  }

}
