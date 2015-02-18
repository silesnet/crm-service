package net.snet.crm.domain.model.agreement;

import spock.lang.Specification

class CustomerTest extends Specification {
  def 'should create a customer'() {
    def customer = new Customer([:])
    expect:
      ! customer.id().exist()
  }

  def 'should create existing customer'() {
    def customer = new Customer(new CustomerId(1), [:])
    expect:
      customer.id().exist()
  }
}
