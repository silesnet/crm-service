package net.snet.crm.infrastructure.network

import net.snet.crm.infrastructure.system.FileSystemCommandFactory
import spock.lang.Specification

class DefaultNetworkServiceTest extends Specification {
  def "should fetch dhcp wireless connection info"() {
    given:
    def commands = new FileSystemCommandFactory(new File('src/test/resources/commands'))
    def network = new DefaultNetworkService(commands, null)
    when:
    def connection = network.fetchDhcpWirelessConnection('lanfiber', '00:15:6D:4A:29:DA').asMap()
    println connection
    then:
    connection.address == '10.110.111.249'
    connection.server == 'lanfiber'
    connection.host == '10306001'
    connection.status == 'bound'
    connection.lastSeen == '16h51m27s'
  }
}
