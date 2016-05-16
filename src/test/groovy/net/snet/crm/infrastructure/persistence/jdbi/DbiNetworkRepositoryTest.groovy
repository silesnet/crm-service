package net.snet.crm.infrastructure.persistence.jdbi
import com.google.common.io.Resources
import net.snet.crm.domain.model.network.NetworkRepository
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

import static net.snet.crm.domain.model.network.NetworkRepository.*

class DbiNetworkRepositoryTest extends Specification {
  @Shared DBI dbi = new DBI("jdbc:h2:mem:customerRepositoryTest")
  @Shared Handle handle

  @Shared NetworkRepository repo

  def setup() {
    repo = new DbiNetworkRepository(dbi)
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
  }

  def 'should find device by id'() {
    given:
      handle.execute("INSERT INTO network (id, name, type, country) VALUES (1, 'a-br', 40, 10)")
    when:
      def device = repo.findDevice(1)
    then:
      with(device) {
        id == 1
        name == 'a-br'
        type == 40
        country == 10
      }
  }

  def 'should return empty map when device not found'() {
    when:
      def device = repo.findDevice(1)
    then:
      device.isEmpty()
  }

  def 'should enable dhcp when record does not exist'() {
    given:
      def service = [id: 11234501, switchId: 15, port: 20]
    when:
      repo.bindDhcp(service.id, service.switchId, service.port)
    then:
      def dhcp = handle.select(
          'SELECT service_id, network_id, port FROM dhcp WHERE network_id=15 AND port=20').first()
      dhcp.service_id == 11234501
      dhcp.network_id == 15
      dhcp.port == 20
  }

  def 'should disable dhcp'() {
    given:
      def service = [id: 11234501, switchId: 15, port: 20]
      handle.insert('INSERT INTO dhcp (service_id, network_id, port) VALUES (11234501, 15, 20)')
    when:
      repo.disableDhcp(service.switchId, service.port)
    then:
      def dhcp = handle.select(
          'SELECT service_id, network_id, port FROM dhcp' +
              ' WHERE network_id=15 AND port=20 ORDER BY service_id').first()
      dhcp.service_id == null
      dhcp.network_id == 15
      dhcp.port == 20
  }

  def 'should find all devices for country'() {
    given:
      handle.execute("INSERT INTO network (id, name, type, country) VALUES (1, 'a-br', 40, 10)")
      handle.execute("INSERT INTO network (id, name, type, country) VALUES (2, 'b-br', 40, 20)")
      handle.execute("INSERT INTO network (id, name, type, country) VALUES (3, 'c', 40, 10)")
      handle.execute("INSERT INTO network (id, name, type, country) VALUES (4, 'd-br', 10, 10)")
    when:
      def devices = repo.findDevicesByCountryAndType(Country.CZ, DeviceType.SWITCH)
    then:
      devices.size() == 1
      devices[0].id == 1
      devices[0].name == 'a-br'
  }

  def cleanup() {
    handle.execute('DROP VIEW service_connections')
    handle.execute('DROP TABLE customers')
    handle.execute('DROP TABLE agreements')
    handle.execute('DROP TABLE services_info')
    handle.execute('DROP TABLE services')
    handle.execute('DROP TABLE connections')
    handle.execute('DROP TABLE users')
    handle.execute('DROP TABLE drafts')
    handle.execute('DROP TABLE drafts2')
    handle.execute('DROP TABLE draft_links')
    handle.execute('DROP TABLE network')
    handle.execute('DROP TABLE dhcp')
    handle.close()
    repo = null
  }
}
