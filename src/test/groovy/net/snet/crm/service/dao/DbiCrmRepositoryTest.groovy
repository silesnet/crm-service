package net.snet.crm.service.dao

import com.google.common.io.Resources
import org.joda.time.DateTime
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class DbiCrmRepositoryTest extends Specification {
  @Shared DBI dbi = new DBI("jdbc:h2:mem:customerRepositoryTest")
  @Shared Handle handle

	@Subject CrmRepository repository

  def setup() {
	  repository = new DbiCrmRepository(dbi)
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
  }

  def cleanup() {
    handle.execute('DROP TABLE customers')
    handle.execute('DROP TABLE agreements')
    handle.execute('DROP TABLE services_info')
    handle.execute('DROP TABLE services')
    handle.execute('DROP TABLE connections')
    handle.execute('DROP TABLE users')
    handle.close()
	  repository = null
  }

  def 'it should insert new customer'() {
    given: 'new customer name'
      def customerName = 'New Name'
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
      !(new DateTime().isBefore(insertedCustomer.inserted_on.getTime() as Long))
    and: 'customers table contains new row'
      handle.select('SELECT count(*) AS cnt FROM customers').first().cnt == 1
  }

	def 'it should delete customer'() {
		given: 'existing customer'
			def customer = repository.insertCustomer([name: 'test'])
		  assert customer != null
		when: 'customer is deleted'
			repository.deleteCustomer(customer.id as Long)
		then: 'customer does not exist in the table'
			handle.select('SELECT count(*) AS cnt FROM customers WHERE id=' + customer.id).first().cnt == 0
	}

  def 'it should find agreements by customer id'() {
    given: 'customers'
      def customer = repository.insertCustomer([name: 'Existing Customer'])
    and: 'agreements'
      def agreement1 = repository.insertAgreement(customer.id as Long, 'CZ')
      def agreement2 = repository.insertAgreement(customer.id as Long, 'CZ')
    when: 'searching for agreements by customer id'
      def agreements = repository.findAgreementsByCustomerId(customer.id as Long)
    then: 'agreements are found'
      agreements.size() == 2
      agreements[0].id == agreement1.id
      agreements[0].customer_id == customer.id
      agreements[1].id == agreement2.id
      agreements[1].customer_id == customer.id
  }

  def 'it should roll back on error inserting customer'() {
    when: 'invalid customer is inserted'
      repository.insertCustomer([name: null])
    then:
      thrown RuntimeException
    and:
      handle.select('SELECT count(*) as cnt FROM customers').first().cnt == 0
  }

  def 'it should insert new customer agreement'() {
    given: 'existing customer'
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
    given: 'existing customer'
      def customer = repository.insertCustomer([name: 'existing customer'])
    and: 'country'
      def country = 'CZ'
    when: 'insert customer agreement for country twice'
      def agreement1 = repository.insertAgreement(customer.id as Long, country)
      def agreement2 = repository.insertAgreement(customer.id as Long, country)
    then: 'agreement is inserted'
      agreement1.id == 100001
      agreement2.id == 100002
    and: 'there are two agreements in the agreements table'
      handle.select('SELECT count(*) AS cnt FROM agreements').first().cnt == 2
    and: 'customer agreements are updated'
//      repository.findCustomerById(1).contract_no == '1, 2'
  }

  def 'it should reuse available agreement id'() {
    given: 'existing existing agreement with AVAILABLE status'
      def customer = repository.insertCustomer([name: 'Test'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
      def availableAgreement = repository.updateAgreementStatus(agreement.id as Long, 'AVAILABLE')
      assert availableAgreement.status.equals('AVAILABLE')
    when: 'adding new agreement'
      def newCustomer = repository.insertCustomer([name: 'Test2'])
      def reusedAgreement = repository.insertAgreement(newCustomer.id as Long, 'CZ')
    then: 'agreement id is reused'
      reusedAgreement.id == availableAgreement.id
    and: 'customer id is populated'
      reusedAgreement.customer_id == newCustomer.id
    and: 'there is only one agreement in the agreements table'
      handle.select('SELECT count(*) AS cnt FROM agreements').first().cnt == 1
  }

	def 'it should update customer agreement status'() {
    given: 'existing agreement'
      def customer = repository.insertCustomer([name: 'John'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
      assert agreement != null
    when: 'agreement status is updated'
      def updatedAgreement = repository.updateAgreementStatus(agreement.id as Long, 'NEW_STATUS')
    then: 'new agreement status can be fetched'
      updatedAgreement.status == 'NEW_STATUS'
  }

  def 'it should insert new agreement service'() {
    given: 'existing customer and agreement'
      def customer = repository.insertCustomer([name: 'existing customer'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
    when: 'insert agreement service'
      def service = repository.insertService(agreement.id as Long)
    then: 'service is inserted'
      service.id == (agreement.id * 100) + 1
      service.customer_id == customer.id
      service.status == 'DRAFT'
  }

  def 'it should fail inserting 100th service for agreement'() {
    given: 'existing customer and agreement with 99 services'
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
    given: 'existing service'
      def customer = repository.insertCustomer([name: 'John'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
      def service = repository.insertService(agreement.id as Long)
      assert service != null
    when: 'service is deleted'
      repository.deleteService(service.id as Long)
    then: 'service does not exist in the tables'
      handle.select('SELECT count(*) AS cnt FROM services WHERE id=' + service.id).first().cnt == 0
      handle.select('SELECT count(*) AS cnt FROM services_info WHERE service_id=' + service.id).first().cnt == 0
	}

  def 'it should insert new service connection'() {
    given: 'existing customer, agreement and service'
      def customer = repository.insertCustomer([name: 'existing customer'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
      def service = repository.insertService(agreement.id as Long)
    when: 'insert service connection'
      def connection = repository.insertConnection(service.id as Long)
    then: 'connection is inserted'
      connection.service_id == service.id
  }

	def 'it should delete service connection'() {
    given: 'existing service connection'
      def customer = repository.insertCustomer([name: 'John'])
      def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
      def service = repository.insertService(agreement.id as Long)
      def connection = repository.insertConnection(service.id as Long)
      assert connection != null
    when: 'service connection is deleted'
      repository.deleteConnection(service.id as Long)
    then: 'service connection does not exist in the table'
      handle.select('SELECT count(*) AS cnt FROM connections WHERE service_id=' + service.id).first().cnt == 0
	}

  def 'it should update service connection'() {
    given: 'existing customer, agreement, service and connection'
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

  def 'it should find user subordinates'() {
    given: 'manager'
      handle.insert("INSERT INTO users(id, login, name, reports_to) VALUES (1, 'manager', 'Manager', 0)")
    and: 'subordinate'
      handle.insert("INSERT INTO users(id, login, name, reports_to) VALUES (2, 'operator', 'Operator', 1)")
    when: 'searching for manager subordinates'
      def subordinates = repository.findUserSubordinates('manager')
    then: 'subordinate is found'
      subordinates.size() == 1
      subordinates[0].id == 2
      subordinates[0].login == 'operator'
      subordinates[0].passwd == null
      subordinates[0].key == null
      subordinates[0].reports_to == null
      subordinates[0].operation_country == 'CZ'
  }

  def 'it should find user by login'() {
    given: 'user'
      handle.insert("INSERT INTO users(id, login, name, reports_to) VALUES (1, 'manager', 'Manager', 0)")
    when: 'searching for user by login name'
      def user = repository.findUserByLogin('manager')
    then: 'user is found'
      user != null
      user.id == 1
      user.login == 'manager'
      user.passwd == null
      user.key == null
      user.reports_to == null
      user.operation_country == 'CZ'
  }

	def 'it should update customer'() {
	  given: 'existing customer'
		  def customer = repository.insertCustomer([name: 'New Customer', country: 20])
	  and: 'customer update map'
			def update = [ name: 'Updated Name', country: 10]
		when: 'customer update is called'
			def updated = repository.updateCustomer(customer.id as Long, update)
		then:
			updated != null
			updated.id == customer.id
			updated.name == 'Updated Name'
			updated.country == 10
	}

	def 'it should update service'() {
		given: 'existing customer'
			def customer = repository.insertCustomer([name: 'New Customer', country: 20])
		and: 'existing agreement'
			def agreement = repository.insertAgreement(customer.id as Long, 'CZ')
		and: 'existing service'
			def service = repository.insertService(agreement.id as Long)
		and: 'service update map'
			def update = [ name: 'Updated Name', price: 10, status: 'ACTIVE']
		expect:
			service.status == 'DRAFT'
		when: 'service update is called'
			def updated = repository.updateService(service.id as Long, update)
		then:
			updated != null
			updated.id == service.id
			updated.name == 'Updated Name'
			updated.price == 10
			updated.status == 'ACTIVE'
	}
}
