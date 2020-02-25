package net.snet.network

import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.tools.jdbc.MockConnection
import org.jooq.tools.jdbc.MockDataProvider
import org.jooq.tools.jdbc.MockExecuteContext
import org.jooq.tools.jdbc.MockResult
import spock.lang.Specification

import java.sql.SQLException
import java.util.function.Consumer

import static net.snet.crm.infra.db.query.tables.Nodes.NODES

class JooqNetworkRepositoryTest extends Specification {
  def "should specify condition"() {
    def sql = ''
    def bindings = []
    def connection = new MockConnection(provider({ MockExecuteContext ctx ->
      bindings = ctx.bindings()
      sql = ctx.sql()
    }))
    def db = DSL.using(connection, SQLDialect.POSTGRES)
    def repository = new JooqNetworkRepository(db)
    def nodes = repository.findNodes(NodeFilter.builder()
        .name("name")
        .master("master")
        .area("area")
        .linkTo("link*")  // wildcard
        .country("CZ")
        .build())
    def parser = db.parser()
    def query = parser.parse(sql, bindings).toString()
    println query
    def condition
    query.eachMatch(/(?s)where \(.*\)/, { where ->
      condition = where
    })
    expect:
      nodes == []
      condition == '''\
        where (
          "query"."nodes"."name" ilike 'name'
          and "query"."nodes"."master" ilike 'master'
          and "query"."nodes"."area" ilike 'area'
          and "query"."nodes"."link_to" ilike 'link%'
          and "query"."nodes"."country" ilike 'CZ'
        )'''.stripIndent()
  }

  def provider(Consumer<?> consumer) {
    return new MockDataProvider() {
      @Override
      MockResult[] execute(MockExecuteContext ctx) throws SQLException {
        consumer.accept(ctx)
        def create = DSL.using(SQLDialect.POSTGRES)
        return [new MockResult(1, create.newResult(NODES))]
      }
    }
  }
}
