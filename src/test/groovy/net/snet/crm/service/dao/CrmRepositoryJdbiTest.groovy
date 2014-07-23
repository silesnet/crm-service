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
    handle.execute(Resources.getResource('db/crm-tables.sql').text)
  }

  def cleanup() {
    handle.execute('DROP TABLE customers')
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
      new DateTime().isAfter(insertedCustomer.inserted_on.getTime())
    and: 'customers table contains new row'
      handle.select('SELECT count(*) as cnt from customers').first().cnt == 1
  }

  def 'it should roll back on error inserting customer'() {
    given: 'repository'
      def repository = new CrmRepositoryJdbi(dbi)
    when: 'invalid customer is inserted'
      repository.insertCustomer([name: null])
    then:
      thrown RuntimeException
    and:
      handle.select('SELECT count(*) as cnt from customers').first().cnt == 0
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
      agreement.id == 1
      agreement.country == 'CZ'
      agreement.customer_id == 1
    and: 'customer agreements are updated'
      repository.findCustomerById(1).contract_no == '1'
  }
}
