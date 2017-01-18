package net.snet.crm.infrastructure.network.access.support

import net.snet.crm.domain.model.network.NetworkRepository
import net.snet.crm.domain.shared.data.MapData
import spock.lang.Specification

class PppoeFactoryTest extends Specification {
  def "should pppoe mapping"() {
    def repository = Stub(NetworkRepository) { repo ->
      repo.findDevice(10) >> [name: 'some-ap', master: 'master']
    }
    def factory = new PppoeFactory(repository)
    def pppoe = factory.pppoeOf(MapData.of(pppoe())).record().asMap()
    expect:
    pppoe.login == 'login'
    pppoe.password == 'password'
    pppoe.mode == 'wireless'
    pppoe.mac.type == 'macaddr'
    pppoe.mac.value == '12'
    pppoe.ip.type == 'inet'
    pppoe.ip.value == '10.0.0.12'
    pppoe.ip_class == 'static'
    pppoe.interface == 'some-ap'
    pppoe.master == 'master'
  }

  def pppoe() {
    [
        data: [
            service_id     : 1234567890,
            auth_a         : 'login',
            auth_b         : 'password',
            product_channel: 'wireless',
            mac_address    : '12',
            ip             : '10.0.0.12',
            ssid           : 10,
            core_router    : 20
        ]
    ]
  }

}
