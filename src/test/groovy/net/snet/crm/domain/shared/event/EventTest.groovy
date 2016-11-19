package net.snet.crm.domain.shared.event

import net.snet.crm.domain.shared.command.CommandId
import net.snet.crm.domain.shared.data.Data
import net.snet.crm.domain.shared.data.MapData
import net.snet.crm.domain.shared.data.MapRecord
import spock.lang.Specification

class EventTest extends Specification {

  def "should parse event data"() {
    expect:
    Event.of(MapRecord.of([
        id: 1,
        event: 'disconnected',
        entity: 'customers',
        entity_id: '1',
        data: '{"key":"value"}',
        command_id: null,
        happened_on: '2016-11-20T10:10:10.001'
    ])).data().stringOf('key') == 'value'
  }

  def "should serialize data map for record"() {
    expect:
    Event.occurred(Events.DISCONNECTED).on("customers", 1).build()
        .recordData().stringOf('data').replaceAll(/\s/, '') == '{}'
    Event.occurred(Events.DISCONNECTED).on("customers", 1)
        .withData(MapData.of([key: 'value']))
        .build()
        .recordData().stringOf('data').replaceAll(/\s/, '') == '{"key":"value"}'
  }

  def "should create simple Event"() {
    expect:
    def event = Event.occurred(Events.DISCONNECTED).on("customers", 1).build()
    event.id().value() == EventId.NONE.value()
    event.name() == Events.DISCONNECTED
    event.entity() == 'customers'
    event.entityId() == 1
    event.data() == Data.EMPTY
    event.commandId() == CommandId.NONE
    sleep(1)
    event.happenedOn().isBeforeNow()
  }

  def "should create simple entity Event"() {
    expect:
      def event = Event.occurred(Events.DISCONNECTED).on("customers").build()
      event.id().value() == EventId.NONE.value()
      event.name() == Events.DISCONNECTED
      event.entity() == 'customers'
      event.entityId() == 0
      event.data() == Data.EMPTY
      event.commandId() == CommandId.NONE
      sleep(1)
      event.happenedOn().isBeforeNow()
  }

  def "should create simple complete Event"() {
    expect:
      def event = Event.occurred(Events.DISCONNECTED).on("customers")
          .withData(MapData.of([key: 'value']))
          .withCommandId(new CommandId(1))
          .build()
      event.id().value() == EventId.NONE.value()
      event.name() == Events.DISCONNECTED
      event.entity() == 'customers'
      event.entityId() == 0
      event.data().asMap() == [key: 'value']
      event.commandId().value() == 1
      sleep(1)
      event.happenedOn().isBeforeNow()
  }


}
