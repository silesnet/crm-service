package net.snet.crm.service.utils

import spock.lang.Specification

import static net.snet.crm.service.utils.Databases.insertSql

class DatabasesTest extends Specification {
  def 'should create insert sql statement'() {
    given:
      def table = 'table'
      def columns = values().keySet()
    when:
      def sql = insertSql(table, columns)
    then:
      sql == 'INSERT INTO table (col1, col2) VALUES (:col1, :col2);'
  }

  def Map values() {
    [
        col1: 'val1',
        col2: 'val2'
    ]
  }
}
