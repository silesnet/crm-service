package net.snet.crm.infrastructure.network.access.support

import net.snet.crm.domain.shared.data.MapData
import spock.lang.Specification

class PppoeTest extends Specification {
  def "should instantiate from data"() {
    expect:
    Pppoe.of(MapData.of([master: 'm', login: 'l'])) != null
  }

  def "should implement equals"() {
    expect:
    left.equals(right) == result
    where:
    left | right | result
    Pppoe.NULL | Pppoe.NULL | true
    Pppoe.NULL | null | false
    pppoe('m', 'l') | Pppoe.NULL | false
    pppoe('m', 'l') | pppoe('m', 'l') | true
    pppoe('m', 'l') | pppoe('m', 'x') | false
  }

  Pppoe pppoe(master, login) {
    Pppoe.of(MapData.of([master: master, login: login, data: [:]]))
  }
}
