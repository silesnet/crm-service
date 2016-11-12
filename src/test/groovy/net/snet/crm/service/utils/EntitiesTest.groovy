package net.snet.crm.service.utils

import org.joda.time.DateTime
import spock.lang.Specification

import javax.ws.rs.WebApplicationException

import static net.snet.crm.service.utils.Entities.*

class EntitiesTest extends Specification {

  def 'should return numeric string property value'() {
    def value = valueOf('prop', [prop: '100'])
    expect:
      value instanceof Value
      !value.isNull()
      value.asLong() == 100 as Long
      value.asInteger() == 100 as Integer
      value.asString() == '100'
      value.toString() == '100'
  }

  def 'should return numeric property value'() {
    def value = valueOf('prop', [prop: 100])
    expect:
      value instanceof Value
      !value.isNull()
      value.asLong() == 100 as Long
      value.asInteger() == 100 as Integer
      value.asString() == '100'
      value.toString() == '100'
  }

  def 'should return date string property value'() {
    def value = valueOf('prop', [prop: '2015-01-13T12:45:55.123'])
    expect:
      value instanceof Value
      !value.isNull()
      value.asDateTime() == DateTime.parse('2015-01-13T12:45:55.123')
  }

  def 'should return default on non existing property value'() {
    def value = valueOf('prop', [:])
    expect:
      value instanceof Value
      value.isNull()
      value.asLongOr(10) == 10 as Long
      value.asIntegerOr(10) == 10 as Integer
      value.asDateTimeOr(null) == null
      value.asStringOr('A') == 'A'
      value.toString() == ''
  }

  def 'should provide long from integer value'() {
    expect:
      valueOf('id', [id: 10], Long.class) instanceof Long
      valueOf('id', [id: 10 as Integer], Long.class) instanceof Long
  }

  def 'should provide value of nested map'() {
    expect:
      valueOf('entities', [entities: [id: 10]], Map.class).id == 10
  }

  def 'should throw when value is null'() {
    when:
      valueOf('entities', [entities: null], Map.class)
    then:
      thrown WebApplicationException
  }

  def 'should throw when path does not exist'() {
    when:
      valueOf('entities', [:], Map.class)
    then:
      thrown WebApplicationException
  }

  def 'should fetch nested value'() {
    expect:
      optionalOf('entities.id', [entities: [id: 10]]).get() == 10
      !optionalOf('entities.name', [entities: [:]]).isPresent()
  }

  def 'should fetch nested map'() {
    expect:
      optionalMapOf('entities', [entities: [id: 10]]).get().id == 10
      !optionalMapOf('entities.name', [entities: [:]]).isPresent()
  }

  def 'should map entity to record'() {
    given:
      def entity = [
          id         : 10,
          entity_name: 'Name',
          entityType : 'Type',
          unknown    : 'foo'
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
    and: 'record preserves order get entity properties'
      record.keySet() as List == ['id', 'entity_type', 'entity_name']
  }

  def 'should map record to entity'() {
    given:
      def record = [
          id         : 10,
          entity_name: 'Name',
          entity_type: 'Type',
          unknown    : 'foo'
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
    and: 'entity preserves order get entity properties'
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
