package net.snet.crm.service.utils

import spock.lang.Specification

import static net.snet.crm.service.utils.Entities.*

class EntitiesTest extends Specification {
  def 'should fetch nested value'() {
    expect:
      fetchNested('entities.id', [entities: [id: 10]]).get() == 10
      !fetchNested('entities.name', [entities: [:]]).isPresent()
  }

  def 'should fetch nested map'() {
    expect:
      fetchNestedMap('entities', [entities: [id: 10]]).get().id == 10
      !fetchNestedMap('entities.name', [entities: [:]]).isPresent()
  }

  def 'should map entity to record'() {
    given:
      def entity = [
          id: 10,
          entity_name: 'Name',
          entityType: 'Type',
          unknown: 'foo'
      ]
    and:
      def mapping = mapping()
    when:
      def record = recordOf(entity, mapping)
    then:
      with(record) {
        id == 10
        entity_type == 'Type'
        entity_name == 'Name'
      }
    and: 'unknown columns are dropped'
      !record.containsKey('unknown')
    and: 'record preserves order of entity properties'
      record.keySet() as List == ['id', 'entity_type', 'entity_name']
  }

  def 'should map record to entity'() {
    given:
      def record = [
          id: 10,
          entity_name: 'Name',
          entity_type: 'Type',
          unknown: 'foo'
      ]
    and:
      def mapping = mapping()
    when:
      def entity = entityOf(record, mapping)
    then:
      with(entity) {
        id == 10
        entityType == 'Type'
        entityName == 'Name'
      }
    and: 'unknown properties are dropped'
      !entity.containsKey('unknown')
    and: 'entity preserves order of entity properties'
      entity.keySet() as List == ['id', 'entityType', 'entityName']
  }

  def mapping() {
    [
        id        : 'id',
        entityType: 'entity_type',
        entityName: 'entity_name'
    ] as LinkedHashMap
  }

}
