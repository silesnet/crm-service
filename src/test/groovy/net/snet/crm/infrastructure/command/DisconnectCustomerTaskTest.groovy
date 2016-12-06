package net.snet.crm.infrastructure.command

import net.snet.crm.domain.model.network.NetworkService
import net.snet.crm.domain.shared.command.Command
import net.snet.crm.domain.shared.command.Commands
import net.snet.crm.domain.shared.event.EventLog
import net.snet.crm.infrastructure.persistence.jdbi.BaseDbiSpecification
import net.snet.crm.infrastructure.persistence.jdbi.DbiEventLog

class DisconnectCustomerTaskTest extends BaseDbiSpecification {

  def "should publish events on successful disconnect"() {
    given:
      def task = new DisconnectCustomerTask(dbi, Mock(NetworkService), command(), new DbiEventLog(dbi))
    when:
      task.perform()
    then:
     handle.select("SELECT count(*) AS cnt FROM events WHERE event='disconnected' AND entity='customers'")[0].cnt == 1
     handle.select("SELECT count(*) AS cnt FROM events WHERE event='disconnected' AND entity='services'")[0].cnt == 2
  }

  def "should fail when command is not disconnect"() {
    given:
      def command = Stub(Command) {
        name() >> Commands.RECONNECT
        entity() >> 'customers'
        entityId() >> 1
      }
    when:
      new DisconnectCustomerTask(dbi, Mock(NetworkService), command, Mock(EventLog))
    then:
      thrown IllegalArgumentException
  }

  def "should fail when command entity is not customers"() {
    given:
    def command = Stub(Command) {
      name() >> Commands.DISCONNECT
      entity() >> 'xxx'
      entityId() >> 1
    }
    when:
      new DisconnectCustomerTask(dbi, Mock(NetworkService), command, Mock(EventLog))
    then:
      thrown IllegalArgumentException
  }

  def "should should call disable service on all customer services"() {
    given:
      def networkService = Mock(NetworkService)
      def task = new DisconnectCustomerTask(dbi, networkService, command(), Mock(EventLog))
    when:
      task.perform()
    then:
      1 * networkService.disableService(1)
      1 * networkService.disableService(3)
  }

  def "should set status to debtor on customer and its services"() {
    given:
      def task = new DisconnectCustomerTask(dbi, Mock(NetworkService), command(), Mock(EventLog))
    when:
      task.perform()
    then:
      handle.select("SELECT status FROM customers WHERE id=1")[0].status == 25
      handle.select("SELECT status FROM services WHERE id=1")[0].status.toString().contains('INHERIT_FROM_CUSTOMER')
      handle.select("SELECT status FROM services WHERE id=2")[0].status.toString().contains('ACTIVE')
      handle.select("SELECT status FROM services WHERE id=3")[0].status.toString().contains('INHERIT_FROM_CUSTOMER')
  }

  def command() {
    Stub(Command) {
      name() >> Commands.DISCONNECT
      entity() >> 'customers'
      entityId() >> 1
    }
  }

  def init() {
    handle.insert("INSERT INTO customers (id, history_id, public_id, name, inserted_on, status) VALUES (1, 1, '1', 'customer', now(), 10)")
    handle.insert("INSERT INTO services (id, customer_id, period_from, name, price) VALUES (1, 1, now(), 'service 1', 100)")
    handle.insert("INSERT INTO services (id, customer_id, period_from, name, price) VALUES (2, 2, now(), 'service 2', 100)")
    handle.insert("INSERT INTO services (id, customer_id, period_from, name, price) VALUES (3, 1, now(), 'service 3', 100)")
  }

}
