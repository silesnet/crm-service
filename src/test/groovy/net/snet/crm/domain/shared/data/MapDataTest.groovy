package net.snet.crm.domain.shared.data

import com.google.common.collect.ImmutableMap
import org.joda.time.DateTime
import org.joda.time.LocalDate
import spock.lang.Specification
import spock.lang.Unroll

class MapDataTest extends Specification {

  def "should return modifiable content"() {
    def data = MapData.of([:])
    when:
    data.asModifiableContent().put('k', 'v')
    then:
    data.stringOf('k') == 'v'
  }

  def "should return data from list of data"() {
    def data = MapData.of([links:
                    [
                        [id: 1, name: 'l1'],
                        [id: 2, name: 'l2']
                    ]
    ])
    expect:
    MapData.of(data.listOf('links').get(1)).stringOf("name") == 'l2'
  }

  def "should return date value"() {
    def data = MapData.of([k: '2017-01-15', l: ''])
    expect:
    data.dateOf('k') == LocalDate.parse('2017-01-15')
    data.optDateOf('l', LocalDate.parse('2017-02-15')) == LocalDate.parse('2017-02-15')
    data.optDateOf('l').minusDays(1).isBefore(LocalDate.now())

    data.dateTimeOf('k') == DateTime.parse('2017-01-15T00:00:00.000')
  }

  def "should detect date value"() {
    def data = MapData.of([k: '2017-01-15', l: ''])
    expect:
    data.hasDate('k')
    data.hasDateTime('k')
    !data.hasDate('l')
    !data.hasDateTime('l')
  }

  def "should detect boolean value"() {
    def data = MapData.of([k: 'true', l: 'x'])
    expect:
    data.hasBoolean('k')
    !data.hasBoolean('l')
  }

  def "should detect list value"() {
    def data = MapData.of([k: [], l: ''])
    expect:
    data.hasList('k')
    !data.hasList('l')
  }

  def "should detect map and data value"() {
    def data = MapData.of([k: [:], l: ''])
    expect:
    data.hasMap('k')
    data.hasData('k')

    !data.hasMap('l')
    !data.hasData('l')
  }

  def "should detect date time value"() {
    def data = MapData.of([k: '', l: '2017-01-15T10:20:30.123'])
    expect:
    data.hasDateTime('l')
    !data.hasDateTime('k')
  }

  def "should should resolve empty string as false"() {
    def data = MapData.of([k: ''])
    expect:
    !data.booleanOf('k')
  }

  def "should detect numeric value"() {
    def data = MapData.of([k: '1', l: 2, m: ''])
    expect:
    data.hasValue('k')
    data.hasValue('l')
    data.hasValue('m')

    data.hasNumber('k')
    data.hasNumber('l')
    !data.hasNumber('m')

    data.intOf('k') == 1
    data.intOf('l') == 2
  }

  def "should convert string to number correctly"() {
    def data = MapData.of([k: '1'])
    expect:
    data.intOf('k') == 1
    data.optIntOf('k') == 1
  }

  def "should return default when has value of wrong type"() {
    def data = MapData.of([k: '', k2: null])
    expect:
    data.hasValue('k')
    !data.hasValue('k2')
    !data.optBooleanOf('k2')
    !data.optBooleanOf('k')
    data.optIntOf('k2') == 0
    data.optIntOf('k') == 0
  }

  def "should return defaults for given types"() {
    def data = MapData.EMPTY
    expect:
    !data.optBooleanOf('k')
    data.optIntOf('k') == 0
    data.optLongOf('k') == 0L
    data.optStringOf('k') == ''
    !data.optDateTimeOf('k').minusMillis(1).isAfterNow()
    data.optDataOf('k').isEmpty()
    data.optMapOf('k').isEmpty()
    data.optListOf('k').isEmpty()
  }

  def "should be able to create new updated data"() {
    def data = MapData.of(ImmutableMap.of('key', 'value', 'key3', 'value3'))
    def map = data.asMap()
    map.put('key2', 'value2')
    map.put('key', 'updated')
    def updated = MapData.of(map)
    expect:
    updated.stringOf('key') == 'updated'
    updated.stringOf('key2') == 'value2'
    updated.stringOf('key3') == 'value3'
  }

  def "should return empty list on non existing path"() {
    def data = MapData.of([:])
    def list = data.optListOf('k')
    expect:
    list.size() == 0
  }

  def "should return list on path"() {
    def data = MapData.of([k: [1, 2]])
    def list = data.listOf('k')
    expect:
    list.size() == 2
  }

  def "should return data on path"() {
    def data = MapData.of([k: [k: 1]]).dataOf('k')
    expect:
    data.asMap() == [k: 1]
  }

  def "should return empty data on non existing path"() {
    def data = MapData.of([:]).optDataOf('k')
    expect:
    data.isEmpty()
  }

  def "should return empty map on non existing path"() {
    def map = MapData.of([:]).optMapOf('k')
    expect:
    map.isEmpty()
  }

  def "should return copy of map data"() {
    def data = MapData.of([k: 1])
    def map = data.asMap()
    map.put('k', 2)
    expect:
    data.longOf('k') == 1
  }

  def "should return map of data"() {
    expect:
    MapData.of([k: 1]).asMap() == [k: 1]
  }

  def "should return map value on path"() {
    expect:
    MapData.of([k: [k: 1]]).mapOf('k') == [k: 1]
  }

  def "should return copy of map value on path"() {
    def data = MapData.of([k: [k: 1]])
    def map = data.mapOf('k')
    map.put('k', 2)
    expect:
    data.longOf('k.k') == 1
  }

  def "should return empty map on no map on path"() {
    expect:
    MapData.of([k: 1]).mapOf('k') == [:]
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
    MapData.of([k: true]).booleanOf('k')
    MapData.of([k: 1]).intOf('k') == 1
    MapData.of([k: 1]).longOf('k') == 1
    MapData.of([k: 1]).stringOf('k') == '1'
    MapData.of([k: '2016-11-11T10:20:50.123']).dateTimeOf('k') == DateTime.parse('2016-11-11T10:20:50.123')
  }

  def "should return default on absent value"() {
    expect:
    MapData.of([:]).optBooleanOf('k', true)
    MapData.of([:]).optIntOf('k', 1) == 1
    MapData.of([:]).optLongOf('k', 1) == 1
    MapData.of([:]).optStringOf('k', '1') == '1'
    MapData.of([:]).optDateTimeOf('k', DateTime.parse('2016-11-11T10:20:50.123')) == DateTime.parse('2016-11-11T10:20:50.123')
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
    map                    | key
    [k: null]              | 'k'
    [:]                    | ''
    [:]                    | 'k'
    [k: 1]                 | ''
    [k: 1]                 | 'x'
    [k: 1]                 | 'k.x'
    [k: [kk: 1]]           | 'k.x'
    [k: [kk: 1]]           | 'k.x.x'
    [k: [kk: 1]]           | 'k.kk.x'
    [k: [kk: null]]        | 'k.kk'
    [k: [kk: [kkk: null]]] | 'k.kk.kkk'
  }

  @Unroll
  def "should test for existing value"() {
    expect:
    MapData.of(map).hasValue(key)
    where:
    map                 | key
    [k: '']             | 'k'
    [k: 1]              | 'k'
    [k: [kk: 1]]        | 'k.kk'
    [k: [kk: [kkk: 1]]] | 'k.kk.kkk'
  }
}
