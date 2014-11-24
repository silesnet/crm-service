package net.snet.crm.service.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

import static net.snet.crm.service.dao.DbiDraftRepository.*

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

  def 'should reuse available typed draft on insert'() {
    given:
      def draftData = createCustomerDraftData()
      def customer = repo.get(repo.createDraft(draftData))
      handle.update("UPDATE $DRAFTS_TABLE SET status='AVAILABLE' WHERE " +
                    "id=:id", customer.id as Long)
    when:
      def draft = repo.get(repo.createDraft(draftData))
    then:
      with(draft) {
        id == customer.id
        status == 'DRAFT'
      }
  }

  def 'should fetch draft with links from repository'() {
    given:
      def customer = repo.get(repo.createDraft(createCustomerDraftData()))
      def draftData = createServiceDraftData()
      draftData.links = ['drafts.customers': customer.entityId]
      def draftId = repo.createDraft(draftData)
    when:
      def draft = repo.get(draftId)
    then:
      draft.links != null
      with(draft.links) {
        get('drafts.customers') == customer.entityId
      }
  }

  def 'should create draft with links in database'() {
    given:
      def customer = repo.get(repo.createDraft(createCustomerDraftData()))
      def draftData = createServiceDraftData()
      draftData.links = ['drafts.customers': customer.entityId]
    when:
      def draftId = repo.createDraft(draftData)
    then:
      def links = handle.select("SELECT * from $DRAFTS_LINKS_TABLE where " +
                                  'draft_id=:draft_id', draftId)
      links.size() > 0
      with(links[0]) {
        draft_id == draftId
        entity == 'drafts.customers'
        entity_id == customer.entityId
      }
  }

  def 'should create draft in database'() {
    given:
      def draftData = createServiceDraftData()
    when:
      def draftId = repo.createDraft(draftData)
    then:
      def draft = handle.select("SELECT * from $DRAFTS_TABLE where id=:id", draftId)
      with(draft[0]) {
        id == draftId
        entity_type == 'services'
        entity_spate == '101234'
        entity_id > 0
        entity_name == 'LanAccess'
        status == 'DRAFT'
        get('owner') == 'test'
        data == '{"name":"LanAccess"}'
      }
  }

  def 'should fetch draft from repository'() {
    given: 'existing draft'
      def draftId = repo.createDraft(createServiceDraftData())
    when:
      def draft = repo.get(draftId)
    then:
      with(draft) {
        id == draftId
        entityType == 'services'
        entitySpate == '101234'
        entityId > 0
        entityName == 'LanAccess'
        status == 'DRAFT'
        get('owner') == 'test'
        data == '{"name":"LanAccess"}'
      }
  }

  def createServiceDraftData() {
    [
        entityType: 'services',
        entitySpate: '101234',
        entityName: 'LanAccess',
        status: 'DRAFT',
        owner: 'test',
        data: [
          name: 'LanAccess'
        ]
    ] as LinkedHashMap
  }

  def createCustomerDraftData() {
    [
        entityType: 'customers',
        entitySpate: '',
        entityName: 'Jan Nowak',
        status: 'DRAFT',
        owner: 'test',
        data: [
            name: 'Jan Nowak'
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
    handle.execute('DROP TABLE draft_links')
    handle.close()
    repo = null
  }

}
