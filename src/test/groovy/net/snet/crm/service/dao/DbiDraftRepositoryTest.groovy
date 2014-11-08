package net.snet.crm.service.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

class DbiDraftRepositoryTest extends Specification {
  @Shared DBI dbi = new DBI("jdbc:h2:mem:customerRepositoryTest")
  @Shared Handle handle

  @Shared
  DbiDraftRepository repo

  def setup() {
    repo = new DbiDraftRepository(dbi, new ObjectMapper())
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
  }

  def 'should create draft in database'() {
    given:
      def draftData = createDraftData()
    when:
      def draftId = repo.createDraft(draftData)
    then:
      def draft = handle.select('SELECT * from drafts2 where id=:id', draftId)
      with(draft[0]) {
        id == draftId
        user == 'test'
        entity_type == 'services'
        entity_spate == '101234'
        entity_id > 0
        entity_name == 'LanAccess'
        status == 'DRAFT'
        data == '{"name":"LanAccess"}'
      }
  }

  def 'should fetch draft from database'() {
    given: 'existing draft'
      def draftId = repo.createDraft(createDraftData())
    when:
      def draft = repo.get(draftId)
    then:
      with(draft) {
        id == draftId
        user == 'test'
        entityType == 'services'
        entitySpate == '101234'
        entityId > 0
        entityName == 'LanAccess'
        status == 'DRAFT'
        data == '{"name":"LanAccess"}'
      }
  }

  def createDraftData() {
    [
        user: 'test',
        entityType: 'services',
        entitySpate: '101234',
        entityId: 0,
        entityName: 'LanAccess',
        status: 'DRAFT',
        data: [
          name: 'LanAccess'
        ]
    ] as LinkedHashMap
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
    repo = null
  }

}
