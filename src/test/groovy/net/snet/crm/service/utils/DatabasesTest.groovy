package net.snet.crm.service.utils
import com.google.common.io.Resources
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

import static net.snet.crm.service.utils.Databases.*

class DatabasesTest extends Specification {
  @Shared
  DBI dbi = new DBI("jdbc:h2:mem:databaseTest")
  @Shared
  Handle handle

  def setup() {
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
    handle.execute('''\
      CREATE TABLE entities (
        id bigint NOT NULL,
        name character varying(80) NOT NULL
      )'''.stripIndent())
  }

  def 'should return next value of sequence'() {
    expect:
      nextValOf('audit_item_id_seq', handle) < nextValOf('audit_item_id_seq', handle)
  }

  def 'should return last id value of empty table'() {
    expect:
      lastValueOf("entities", "id", handle) == 0
  }

  def 'should return last id value'() {
    handle.execute("INSERT INTO entities (id, name) VALUES (1, 'name');")
    handle.execute("INSERT INTO entities (id, name) VALUES (2, 'name2');")
    expect:
      lastValueOf("entities", "id", handle) == 2
  }

  def 'should create update sql statement'() {
    given:
      def table = 'table'
      def columns = values().keySet()
    when:
      def sql = updateSql(table, columns)
    then:
      sql == 'UPDATE table SET col1=:col1, col2=:col2 WHERE id=:id;'
  }

  def 'should create update sql statement with id'() {
    given:
      def table = 'table'
      def columns = values().keySet()
    when:
      def sql = updateSqlWithId(table, 'col_id', columns)
    then:
      sql == 'UPDATE table SET col1=:col1, col2=:col2 WHERE col_id=:col_id;'
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

  def 'should fail when creating insert for illegal table name'() {
    given:
      def table = ';drop'
    when:
      insertSql(table, [] as Set)
    then:
      thrown(RuntimeException)
  }

  def 'should find last entity id when core table is empty'() {
    given:
      def entityType = 'entities'
      def entitySpate = ''
    when:
      def entityId = lastEntityIdFor(entityType, entitySpate, handle)
    then:
      entityId == 0
  }

  def 'should find last entity id when core table has records'() {
    given: 'empty core table'
      def entityType = 'entities'
      def entitySpate = ''
      handle.execute("INSERT INTO entities (id, name) VALUES (1, 'name');")
    when:
      def entityId = lastEntityIdFor(entityType, entitySpate, handle)
    then:
      entityId == 1
  }

  def 'should find last entity id when draft exist'() {
    given: 'draft record for entity'
      def entityType = 'entities'
      def entitySpate = 'CZ'
      handle.execute('''\
        INSERT INTO drafts2 (entity_type, entity_spate, entity_id, entity_name,
          status, owner, data)
        VALUES ('entities', 'CZ', 7, 'name', 'DRAFT', 'test', '{}');'''.stripIndent())
    when:
      def entityId = lastEntityIdFor(entityType, entitySpate, handle)
    then:
      entityId == 7
  }

  def cleanup() {
    handle.execute(Resources.getResource('db/h2-drop-crm-tables.sql').text)
    handle.close()
  }

  def Map values() {
    [
        col1: 'val1',
        col2: 'val2'
    ]
  }
}
