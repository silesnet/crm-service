package net.snet.crm.service.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import com.google.common.io.Resources
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

import static net.snet.crm.service.dao.DbiDraftRepository.DRAFTS_LINKS_TABLE
import static net.snet.crm.service.dao.DbiDraftRepository.DRAFTS_TABLE

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

  def 'should delete draft'() {
    given: 'draft with links'
      def draftData = createServiceDraftData()
      draftData.links = ['customers': 100]
      def draftId = repo.create(draftData)
    when:
      repo.delete(draftId)
    then:
      handle.select("SELECT * FROM ${DRAFTS_TABLE} WHERE id=${draftId}")
          .size() == 0
      handle.select("SELECT * FROM ${DRAFTS_LINKS_TABLE} WHERE draft_id=${draftId}")
          .size() == 0
  }

  def 'should update draft'() {
    given: 'draft with links'
      def draftData = createServiceDraftData()
      draftData.links = ['customers': 100]
      def draft = repo.get(repo.create(draftData))
    and: 'draft update'
      def draftUpdate = Maps.newHashMap(draft)
      draftUpdate.entityType = 'not change'
      draftUpdate.entityId = -1
      draftUpdate.entityName = 'Updated Name'
      draftUpdate.status = 'OK'
      draftUpdate.put('owner', 'foo')
      draftUpdate.data = [name: 'name']
      draftUpdate.links = ['services': 200]
    when:
      repo.update(draft.id as Long, draftUpdate as Map)
    then:
      def updated = repo.get(draft.id as Long)
      with(updated) {
        entityType == draft.entityType // NO CHANGE
        entityId == draft.entityId // NO CHANGE
        entityName == draftUpdate.entityName
        status == draftUpdate.status
        data == draftUpdate.data
        get('owner') == draftUpdate.get('owner')
        links.customers == null // REMOVED
        links.services == 200
      }
  }

  def 'should reuse available typed draft on insert'() {
    given:
      def draftData = createCustomerDraftData()
      def customer = repo.get(repo.create(draftData))
      handle.update("UPDATE $DRAFTS_TABLE SET status='AVAILABLE' WHERE " +
                    "id=:id", customer.id as Long)
    when:
      def draft = repo.get(repo.create(draftData))
    then:
      with(draft) {
        id == customer.id
        status == 'DRAFT'
      }
  }

  def 'should fetch draft with links from repository'() {
    given:
      def customer = repo.get(repo.create(createCustomerDraftData()))
      def draftData = createServiceDraftData()
      draftData.links = ['drafts.customers': customer.entityId]
      def draftId = repo.create(draftData)
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
      def customer = repo.get(repo.create(createCustomerDraftData()))
      def draftData = createServiceDraftData()
      draftData.links = ['drafts.customers': customer.entityId]
    when:
      def draftId = repo.create(draftData)
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
      def draftId = repo.create(draftData)
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
      def draftId = repo.create(createServiceDraftData())
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
        data == [name: 'LanAccess']
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
    handle.execute(Resources.getResource('db/h2-drop-crm-tables.sql').text)
    handle.close()
    repo = null
  }

}
