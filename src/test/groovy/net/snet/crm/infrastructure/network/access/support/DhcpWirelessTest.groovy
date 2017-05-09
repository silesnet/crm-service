package net.snet.crm.infrastructure.network.access.support

import net.snet.crm.domain.shared.data.Data
import net.snet.crm.domain.shared.data.MapData
import spock.lang.Specification
import spock.lang.Unroll

class DhcpWirelessTest extends Specification {
  def "should instantiate from data"() {
    expect:
    DhcpWireless.of(Data.EMPTY) != null
    DhcpWireless.of(null) != null
  }

  def "should implement equals"() {
    expect:
    println "$left $right"
    left.equals(right) == result
    where:
    left | right | result
    DhcpWireless.NULL | DhcpWireless.NULL | true
    dhcpWireless([:]) | DhcpWireless.NULL | true
    DhcpWireless.of(null) | DhcpWireless.NULL | true
    DhcpWireless.NULL | null | false
    dhcpWireless([ip: 1]) | dhcpWireless([ip:1]) | true
    dhcpWireless([ip: 1]) | dhcpWireless([ip:2]) | false
  }

  DhcpWireless dhcpWireless(map) {
    DhcpWireless.of(MapData.of(map))
  }
}
