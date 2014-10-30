package net.snet.crm.service.utils

import com.google.common.io.Resources
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

import static net.snet.crm.service.utils.Databases.*

class DatabasesTest extends Specification {
  @Shared DBI dbi = new DBI("jdbc:h2:mem:databaseTest")
  @Shared Handle handle

  def setup() {
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
    handle.execute('CREATE TABLE e (\n' +
        '    id bigint NOT NULL,\n' +
        '    history_id bigint NOT NULL,\n' +
        '    public_id character varying(20) NOT NULL,\n' +
        '    name character varying(80) NOT NULL,')
  }

  def 'should create insert sql statement'() {
    given:
      def table = 'table'
      def columns = values().keySet()
    when:
      def sql = insertSql(table, columns)
    then:
      sql == 'INSERT INTO table (col1, col2) VALUES (:col1, :col2);'
  }

  def 'should find next entity id when core table is empty'() {
    given: 'empty core table'
      def table = 'customers'
    when:
      def entityId = nextEntityIdFor(table, handle)
    then:
      entityId == 1
  }

  def 'should find next entity id when core table has records'() {
    given: 'empty core table'
      def table = 'customers'
      handle.execute("INSERT INTO customers (id, history_id, name, public_id, inserted_on) VALUES (1, 1, 'name', '9', now());")
    when:
      def entityId = nextEntityIdFor(table, handle)
    then:
      entityId == 1
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

  def Map values() {
    [
        col1: 'val1',
        col2: 'val2'
    ]
  }
}
