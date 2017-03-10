package net.snet.crm.infrastructure.network.access.support

import net.snet.crm.domain.shared.data.MapData
import spock.lang.Specification

class DhcpTest extends Specification {
  def "should instantiate from fields"() {
    def dhcp = Dhcp.of(100, 20, 'switch')
    expect:
    dhcp.switchId() == 100
    dhcp.port() == 20
    dhcp.switchName() == 'switch'
    Dhcp.of(0, 0, 'switch') != Dhcp.NULL
  }

  def "should instantiate from data"() {
    def dhcp = Dhcp.of(MapData.of([switchId: 100, port: 20, switch: 'switch']))
    expect:
    dhcp.switchId() == 100
    dhcp.port() == 20
    dhcp.switchName() == 'switch'
    Dhcp.of(MapData.of([switchId: 0, port: 0, switch: 's'])) != Dhcp.NULL
  }

  def "has NULL instance"() {
    expect:
      Dhcp.NULL != null
  }

  def "should return NULL instance on invalid field data"() {
    expect:
    Dhcp.of(-100, 10, 's') == Dhcp.NULL
    Dhcp.of(100, -10, 's') == Dhcp.NULL
    Dhcp.of(100, 10, '') == Dhcp.NULL
    Dhcp.of(100, 10, ' ') == Dhcp.NULL
    Dhcp.of(100, 10, null) == Dhcp.NULL
  }

  def "should return NULL instance on invalid data"() {
    expect:
    Dhcp.of(MapData.of([switchId: -100, port: 10, switch: 's'])) == Dhcp.NULL
    Dhcp.of(MapData.of([switchId: 100, port: -10, switch: 's'])) == Dhcp.NULL
    Dhcp.of(MapData.of([switchId: 100, port: 10, switch: ''])) == Dhcp.NULL
    Dhcp.of(MapData.of([switchId: 100, port: 10, switch: ' '])) == Dhcp.NULL
    Dhcp.of(MapData.of([switchId: 100, port: 10, switch: null])) == Dhcp.NULL
    Dhcp.of(MapData.of([:])) == Dhcp.NULL
  }

  def "should implement equals"() {
    expect:
    left.equals(right) == result
    where:
    left | right | result
    Dhcp.of(10, 20, 's') | Dhcp.of(10, 20,'s') | true
    Dhcp.NULL | Dhcp.NULL | true
    Dhcp.of(10, 20, 's') | Dhcp.of(10, 20,'x') | false
    Dhcp.of(10, 20, 's') | Dhcp.NULL | false
    Dhcp.NULL | Dhcp.of(10, 20,'s') | false
    Dhcp.NULL | null | false

  }

}
