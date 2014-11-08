package net.snet.crm.service.dao

import com.google.common.io.Resources
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

import static net.snet.crm.service.dao.EntityIdFactory.entityIdFor

class EntityIdFactoryTest extends Specification {
  @Shared
  DBI dbi = new DBI("jdbc:h2:mem:databaseTest")
  @Shared
  Handle handle

  def setup() {
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
  }

  def 'should find next customer id when no customer nor draft exist'() {
    given:
      def entity = 'customers'
      def spate = ''
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 1
  }

  def 'should find next customer id when customer exist but draft does not'() {
    given:
      def entity = 'customers'
      def spate = ''
      insertCustomerOf(6)
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 7
  }

  def 'should find next customer id when customer and draft exist'() {
    given:
      def entity = 'customers'
      def spate = ''
      insertCustomerOf(6)
      insertDraftOf(entity, '', 7, 'name')
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 8
  }

  def 'should find next CZ agreement id when no agreement nor draft exist'() {
    given:
      def entity = 'agreements'
      def spate = 'CZ'
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 100001
  }

  def 'should find next PL agreement id when no agreement nor draft exist'() {
    given:
      def entity = 'agreements'
      def spate = 'PL'
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 200001
  }

  def 'should find next agreement id when agreements exist but draft does not'() {
    given:
      def entity = 'agreements'
      def spate = 'CZ'
      insertAgreementOf(6, 'CZ')
      insertAgreementOf(7, 'PL')
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 7
  }

  def 'should find next agreement id when agreements and draft exist'() {
    given:
      def entity = 'agreements'
      def spate = 'CZ'
      insertAgreementOf(6, 'CZ')
      insertAgreementOf(8, 'PL')
      insertDraftOf(entity, 'CZ', 7, 'name')
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 8
  }

  def 'should find next service id when no service nor draft exist'() {
    given:
      def entity = 'services'
      def spate = '100567'
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 10056701
  }

  def 'should find next service id when services exist but draft does not'() {
    given:
      def entity = 'services'
      def spate = '100567'
      insertServiceOf(10056701)
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 10056702
  }

  def 'should find next service id when services and draft exist'() {
    given:
      def entity = 'services'
      def spate = '100567'
      insertServiceOf(10056701)
      insertDraftOf(entity, spate, 10056702, 'LAN')
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 10056703
  }

  def 'should find next CZ service id when noise PL services exist'() {
    given:
      def entity = 'services'
      def spate = '100567'
      insertServiceOf(20056701)
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 10056701
  }

  def 'should find next PL service id when noise CZ services exist'() {
    given:
      def entity = 'services'
      def spate = '200567'
      insertServiceOf(10056701)
    when:
      def nextId = entityIdFor(entity, spate, handle).nextId()
    then:
      nextId == 20056701
  }

  def 'should fail when 99 agreement services exist'() {
    given:
      def entity = 'services'
      def spate = '100567'
      insertServiceOf(10056799)
    when:
      println entityIdFor(entity, spate, handle).nextId()
    then:
      thrown(RuntimeException)
  }

  def insertDraftOf(entityType, entitySpate, entityId, entityName) {
    handle.execute("""\
      INSERT INTO drafts2
        (user, entity_type, entity_spate, entity_id, entity_name, status, data)
      VALUES
        ('test', '${entityType}', '${entitySpate}', ${entityId},
         '${entityName}', 'DRAFT', '{}');""".stripIndent()
    )
  }

  def insertCustomerOf(customerId) {
    handle.execute("""\
      INSERT INTO customers
        (id, history_id, public_id, name, inserted_on)
      VALUES
        (${customerId}, 1, '1', 'customer', '2014-11-01');""".stripIndent()
    )
  }

  def insertAgreementOf(agreementId, country) {
    handle.execute("""\
      INSERT INTO agreements
        (id, country)
      VALUES
        (${agreementId}, '${country}');""".stripIndent()
    )
  }

  def insertServiceOf(serviceId) {
    handle.execute("""\
      INSERT INTO services
        (id, period_from, name, price)
      VALUES
        (${serviceId}, '2014-11-01', 'LANAccess', 100);""".stripIndent()
    )
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
    handle.close()
  }
}
