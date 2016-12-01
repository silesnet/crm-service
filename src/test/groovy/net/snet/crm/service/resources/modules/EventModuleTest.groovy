package net.snet.crm.service.resources.modules

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import net.snet.crm.domain.shared.command.CommandId
import net.snet.crm.domain.shared.data.MapData
import net.snet.crm.domain.shared.event.Event
import net.snet.crm.domain.shared.event.Events
import spock.lang.Specification

class EventModuleTest extends Specification {
  ObjectMapper mapper = new ObjectMapper()

  def setup() {
    mapper.registerModule(new EventModule())
  }

  def "should map event to json"() {
    def event = new Event(Events.DISCONNECTED, 'customers', 10, MapData.of([key: 'value']), CommandId.NONE)
    def json = new JsonSlurper().parseText(mapper.writeValueAsString(event))
    expect:
      json.type == 'events'
      json.id == '0'
      json.attributes.event == 'disconnected'
      json.attributes.entity == 'customers'
      json.attributes.entityId == 10
      json.attributes.commandId == 0
      json.attributes.happenedOn.beforeNow
      json.attributes.data == [key: 'value']
  }
}
