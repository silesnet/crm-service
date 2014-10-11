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
}
