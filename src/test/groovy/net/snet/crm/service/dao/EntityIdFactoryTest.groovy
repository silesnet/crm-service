package net.snet.crm.service.dao

import com.google.common.io.Resources
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

import static net.snet.crm.service.utils.Databases.insertSql
import static net.snet.crm.service.utils.Databases.nextEntityIdFor

class EntityIdFactoryTest extends Specification {
  @Shared
  DBI dbi = new DBI("jdbc:h2:mem:databaseTest")
  @Shared
  Handle handle

  def setup() {
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
  }

  def 'should find next customers id when draft exist'() {
    given: 'customers draft record'
      def table = 'customers'
      insertDraftOf(table, 7, 'name')
    when:
      def entityId = EntityIdFactory.entityIdFor(table, handle).nextEntityId()
    then:
      entityId == 8
  }

  def 'should find next agreement id when draft exist'() {
    given: 'agreements draft record'
      def table = 'agreements'
      insertDraftOf(table, 100012, 'CZ')
      insertDraftOf(table, 200012, 'PL')
    when:
      def entityId = EntityIdFactory.entityIdFor("${table}.CZ",
          handle).nextEntityId()
    then:
      entityId == 100013
  }

  def 'should find next services id for agreement when no draft nor service exist'() {
    given:
      def table = 'services'
      def agreement = '100012'
    when:
      def entityId = EntityIdFactory.entityIdFor("${table}.${agreement}", handle)
                        .nextEntityId()
    then:
      entityId == 10001201
  }

  def 'should find next services id for agreement when service exist but not draft'() {
    given:
      def table = 'services'
      def agreement = '100012'
      handle.execute("INSERT INTO services(id, period_from, name, price) " +
          "VALUES (10001201, '2014-11-01', 'LANAccess', 100);")
    when:
      def entityId = EntityIdFactory.entityIdFor("${table}.${agreement}", handle)
                        .nextEntityId()
    then:
      entityId == 10001202
  }


  def insertDraftOf(entityType, entityId, entityName) {
    handle.execute("INSERT INTO drafts2 (user, entity_type, entity_id, " +
        "entity_name, status, data) VALUES ('test', '${entityType}', ${entityId}, " +
        "'${entityName}', 'DRAFT', '{}');")
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
