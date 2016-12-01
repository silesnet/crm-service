package net.snet.crm.domain.shared.event

import spock.lang.Specification

class EventConstrainTest extends Specification {

  def "create all events constrain as empty"() {
    def constrain = EventConstrain.builder().build()
    expect:
      constrain.sql() == ''
      constrain.binding().isEmpty()
  }

  def "create past event id constrain"() {
    def constrain = EventConstrain.builder()
        .eventsPastEventId(0)
        .build()
    def sql = constrain.sql().replaceAll(' ', '')
    expect:
      sql.contains('id>:id')
      sql.split(/AND/).size() == 1
      constrain.binding() == [id: 0]
  }

  def "crash on negative event id"() {
    when:
    EventConstrain.builder()
        .eventsPastEventId(-1)
    then:
      thrown IllegalArgumentException
  }

  def "create event constrain"() {
    def constrain = EventConstrain.builder()
        .forEvent(Events.DISCONNECTED)
        .build()
    def sql = constrain.sql().replaceAll(' ', '')
    expect:
      sql.contains("event=:event")
      sql.split(/AND/).size() == 1
      constrain.binding() == [event: 'disconnected']
  }

  def "crash on null event constrain"() {
    when:
      EventConstrain.builder()
          .forEvent(null)
    then:
      thrown IllegalArgumentException
  }

  def "create entity constrain"() {
    def constrain = EventConstrain.builder()
        .forEntity('services')
        .build()
    def sql = constrain.sql().replaceAll(' ', '')
    expect:
      sql.contains("entity=:entity")
      sql.split(/AND/).size() == 1
      constrain.binding() == [entity: 'services']
  }

  def "crash on null entity"() {
    when:
    EventConstrain.builder()
        .forEntity(null)
    then:
      thrown IllegalArgumentException
  }

  def "create entity instance constrain"() {
    def constrain = EventConstrain.builder()
        .forEntityInstance('services', 1)
        .build()
    def sql = constrain.sql().replaceAll(' ', '')
    expect:
      sql.contains("entity=:entity")
      sql.contains("entity_id=:entity_id")
      sql.split(/AND/).size() == 2
      constrain.binding() == [entity: 'services', entity_id: 1]
  }

  def "crash on null entity instance"() {
    when:
    EventConstrain.builder()
        .forEntityInstance(null, 1)
    then:
      thrown IllegalArgumentException
  }

  def "crash on zero or negative entity instance id"() {
    when:
    EventConstrain.builder()
        .forEntityInstance('services', 0)
    then:
      thrown IllegalArgumentException
  }

  def "create combined constrain"() {
    def constrain = EventConstrain.builder()
        .eventsPastEventId(10)
        .forEntityInstance('services', 123)
        .forEvent(Events.DISCONNECTED)
        .build()
    def sql = constrain.sql().replaceAll(' ', '')
    expect:
      sql.contains('id>:id')
      sql.contains("event=:event")
      sql.contains("entity=:entity")
      sql.contains("entity_id=:entity_id")
      sql.split(/AND/).size() == 4
      constrain.binding() == [id: 10L, event: 'disconnected', entity: 'services', entity_id: 123L]
  }

  def "remove entity id constrain when entity called after"() {
    def constrain = EventConstrain.builder()
        .forEntityInstance('services', 1)
        .forEntity('services')
        .build()
    def sql = constrain.sql().replaceAll(' ', '')
    expect:
      !sql.contains("entity_id")
      !constrain.binding().containsKey("entity_id")
  }
}
