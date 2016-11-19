package net.snet.crm.infrastructure.persistence.jdbi

import net.snet.crm.domain.shared.event.Event
import net.snet.crm.domain.shared.event.EventId
import net.snet.crm.domain.shared.event.EventLog
import net.snet.crm.service.utils.JsonUtil
import spock.lang.Shared

import static net.snet.crm.domain.shared.event.Events.DISCONNECTED
import static net.snet.crm.domain.shared.event.Events.RECONNECTED

class DbiEventLogTest extends BaseDbiSpecification {
  @Shared EventLog eventLog

  def "should find event"() {
    given:
      def published = eventLog.publish(event())
    when:
      def found = eventLog.find(published.id())
    then:
      found.id() == published.id()
  }

  def "should find past events"() {
    given:
      eventLog.publish(event())
      eventLog.publish(event())
    when:
      def events = eventLog.eventsPast(new EventId(0), 10)
    then:
      events.size() == 2
      events[0].id().value() == 1
      events[1].id().value() == 2
  }

  def "should find past events of certain type"() {
    given:
      eventLog.publish(Event.occurred(DISCONNECTED).on('customers').build())
      eventLog.publish(Event.occurred(RECONNECTED).on('customers').build())
    when:
      def events = eventLog.eventsPast(new EventId(0), RECONNECTED, 10)
    then:
      events.size() == 1
      events[0].id().value() == 2
  }

  def "should find past events of certain entity"() {
    given:
    eventLog.publish(Event.occurred(DISCONNECTED).on('customers', 1).build())
    eventLog.publish(Event.occurred(RECONNECTED).on('customers', 2).build())
    when:
    def events = eventLog.eventsPast(new EventId(0), 'customers', 2, 10)
    then:
    events.size() == 1
    events[0].id().value() == 2
  }

  def "should publish event"() {
    when:
      def published = eventLog.publish(event())
    then:
    def record = handle.select("SELECT * FROM events WHERE id=1")[0]
    published.id().value() == record.id as long
    published.id().value() == 1
    published.name().event() == record.event
    published.name() == DISCONNECTED
    published.entity() == record.entity
    published.entity() == 'services'
    published.entityId() == record.id as long
    published.entityId() == 1
    published.data().asMap() == JsonUtil.dataOf(record.data).asMap()
    published.data().asMap() == [key: 'value']
    published.commandId().value() == record.command_id as long
    sleep(1)
    published.happenedOn().isBeforeNow()
  }

  private Event event() {
    Event.occurred(DISCONNECTED).on('services', 1)
        .withData([key: 'value'])
        .withCommandId(1)
        .build()
  }

  def init() {
    eventLog = new DbiEventLog(dbi)
  }
}
