package net.snet.crm.domain.shared.event

import spock.lang.Specification

class EventConstrainTest extends Specification {
  def "should create all events constrain"() {
    expect:
      EventConstrain.builder().build().sql() == ''
  }

  def "should create past event id  constrain"() {
    expect:
      EventConstrain.builder()
        .eventsPastEventId(10)
        .build().sql().replaceAll(' ', '').contains('id>10')
  }

  def "should create entity constrain"() {
    expect:
      EventConstrain.builder()
        .forEntity('services')
        .build().sql().replaceAll(' ', '').contains("entity='services'")
  }

  def "should create entity id constrain"() {
    expect:
      EventConstrain.builder()
        .forEntityInstance('services', 123)
        .build().sql().replaceAll(' ', '').contains("entity='services'ANDentity_id=123")
  }

  def "should create event constrain"() {
    expect:
    EventConstrain.builder()
        .forEvent(Events.DISCONNECTED)
        .build().sql().replaceAll(' ', '').contains("event='disconnected'")
  }

  def "should create combined constrain"() {
    def constrain = EventConstrain.builder()
        .eventsPastEventId(10)
        .forEntityInstance('services', 123)
        .forEvent(Events.DISCONNECTED)
        .build().sql().replaceAll(' ', '')
    expect:
      constrain.contains('id>10')
      constrain.contains("event='disconnected'")
      constrain.contains("entity='services'")
      constrain.contains("entity_id=123")
      constrain.split(/AND/).size() == 4
  }

  def "should remove entity id constrain when entity called after"() {
    expect:
    !EventConstrain.builder()
        .forEntityInstance('services', 1)
        .forEntity('services')
        .build().sql().replaceAll(' ', '').contains("entity_id")
  }
}
