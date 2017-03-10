package net.snet.crm.infrastructure.network.access.support

import net.snet.crm.domain.shared.data.Data
import net.snet.crm.domain.shared.data.MapData
import spock.lang.Specification

class DhcpWirelessTest extends Specification {
  def "should instantiate from data"() {
    expect:
    DhcpWireless.of(Data.EMPTY) != null
    DhcpWireless.of(null) != null
  }

  def "should implement equals"() {
    expect:
    left.equals(right) == result
    where:
    left | right | result
    DhcpWireless.NULL | DhcpWireless.NULL | true
    dhcpWireless([:]) | DhcpWireless.NULL | true
    DhcpWireless.of(null) | DhcpWireless.NULL | true
    DhcpWireless.NULL | null | false
    dhcpWireless([a: 1]) | dhcpWireless([a:1]) | true
    dhcpWireless([a: 1]) | dhcpWireless([a:2]) | false
  }

  DhcpWireless dhcpWireless(map) {
    DhcpWireless.of(MapData.of(map))
  }
}
