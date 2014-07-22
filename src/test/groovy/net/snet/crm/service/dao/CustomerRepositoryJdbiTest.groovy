package net.snet.crm.service.dao

import com.google.common.io.Resources
import org.joda.time.DateTime
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

class CustomerRepositoryJdbiTest extends Specification {
  @Shared DBI dbi = new DBI("jdbc:h2:mem:customerRepositoryTest")
  @Shared Handle handle

  def setup() {
    handle = dbi.open()
    handle.execute(Resources.getResource('db/customers-table.sql').text)
  }

  def cleanup() {
    handle.execute('DROP TABLE customers')
    handle.close()
  }

  def 'it should insert new customer'() {
    given: 'new customer name'
      def customerName = 'New Name'
    and: 'customer repository'
      def repository = new CustomerRepositoryJdbi(dbi)
    and: 'customers table is empty'
      assert handle.select('SELECT count(*) as cnt from customers').first().cnt == 0
    when: 'new customers is inserted into repository'
      def insertedCustomer = repository.insert([name: customerName])
    then: 'inserted customer is populated from database'
      insertedCustomer.id != 1
      insertedCustomer.history_id == 1
      insertedCustomer.name == customerName
      insertedCustomer.public_id == '9999999'
      insertedCustomer.is_active == false
      new DateTime().isAfter(insertedCustomer.inserted_on.getTime())
    and: 'customers table contains new row'
      handle.select('SELECT count(*) as cnt from customers').first().cnt == 1
  }
}
