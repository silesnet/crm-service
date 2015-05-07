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
    handle.execute('DROP TABLE customers')
    handle.execute('DROP TABLE agreements')
    handle.execute('DROP TABLE services_info')
    handle.execute('DROP TABLE services')
    handle.execute('DROP TABLE connections')
    handle.execute('DROP TABLE users')
    handle.execute('DROP TABLE drafts')
    handle.execute('DROP TABLE drafts2')
    handle.execute('DROP TABLE draft_links')
    handle.close()
    repo = null
  }
}
