package net.snet.crm.domain.shared.data

import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Unroll

class MapDataTest extends Specification {

  def "should return copy of map data"() {
    when:
      def data = MapData.of([k: 1])
      def map = data.asMap()
      map.put('k', 2)
    then:
      data.longOf('k') == 1
  }

  def "should return map of data"() {
    expect:
      MapData.of([k : 1]).asMap() == [k : 1]
  }

  def "should return map value on path"() {
    expect:
      MapData.of([k : [k: 1]]).mapOf('k') == [k : 1]
  }

  def "should return copy of map value on path"() {
    when:
      def data = MapData.of([k: [k: 1]])
      def map = data.mapOf('k')
      map.put('k', 2)
    then:
      data.longOf('k.k') == 1
  }

  def "should return empty map on no map on path"() {
    expect:
      MapData.of([k : 1]).mapOf('k') == [:]
  }

  def "should throw on null passed to factory method"() {
    when:
      MapData.of(null)
    then:
      thrown IllegalArgumentException
  }

  def "should throw on direct fetching of absent vale"() {
    when:
      MapData.of([:]).longOf('k')
    then:
      thrown IllegalArgumentException
  }

  def "should return value on path"() {
    expect:
      MapData.of([k : true]).booleanOf('k')
      MapData.of([k : 1]).intOf('k') == 1
      MapData.of([k : 1]).longOf('k') == 1
      MapData.of([k : 1]).stringOf('k') == '1'
      MapData.of([k : '2016-11-11T10:20:50.123']).dateTimeOf('k') == DateTime.parse('2016-11-11T10:20:50.123')
  }

  def "should return default on absent value"() {
    expect:
      MapData.of([:]).optionalBooleanOf('k', true)
      MapData.of([:]).optionalIntOf('k', 1) == 1
      MapData.of([:]).optionalLongOf('k', 1) == 1
      MapData.of([:]).optionalStringOf('k', '1') == '1'
      MapData.of([:]).optionalDateTimeOf('k', DateTime.parse('2016-11-11T10:20:50.123')) == DateTime.parse('2016-11-11T10:20:50.123')
  }

  def "should throw on null path"() {
    when:
      MapData.of([:]).hasValue(null)
    then:
      thrown IllegalArgumentException
  }

  @Unroll
  def "should test for absent value"() {
    expect:
      !MapData.of(map).hasValue(key)
    where:
      map | key
      [:] | ''
      [:] | 'k'
      [k: 1] | ''
      [k: 1] | 'x'
      [k: 1] | 'k.x'
      [k: [kk: 1]] | 'k.x'
      [k: [kk: 1]] | 'k.x.x'
      [k: [kk: 1]] | 'k.kk.x'
  }

  @Unroll
  def "should test for existing value"() {
    expect:
      MapData.of(map).hasValue(key)
    where:
      map | key
      [k: null] | 'k'
      [k: 1] | 'k'
      [k: [kk: 1]] | 'k.kk'
      [k: [kk: null]] | 'k.kk'
      [k: [kk: [kkk: 1]]] | 'k.kk.kkk'
      [k: [kk: [kkk: null]]] | 'k.kk.kkk'
  }
}
