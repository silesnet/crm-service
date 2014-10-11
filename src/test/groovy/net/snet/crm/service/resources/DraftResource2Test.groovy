package net.snet.crm.service.resources

import ch.qos.logback.classic.Level
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.PartialRequestBuilder
import io.dropwizard.logging.LoggingFactory
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.dao.DraftRepository
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.core.MediaType


class DraftResource2Test extends Specification {

  public static final long CUSTOMER_DRAFT_ID = 10L
  @Shared
  DraftResource2 draftResource = new DraftResource2(null);

  @Shared
  @ClassRule
  ResourceTestRule testRule = ResourceTestRule.builder().addResource(draftResource).build()

  def setupSpec() {
    LoggingFactory.bootstrap(Level.DEBUG)
  }

  def 'should create new customer draft resource'() {
    given: 'customer POST draft data'
      def postData = createCustomerDraftPostData()
    and: 'draft repository'
      def draftRepository = wiredDraftRepositoryStub()
      draftRepository.createDraft(_) >> CUSTOMER_DRAFT_ID
      draftRepository.get(CUSTOMER_DRAFT_ID) >> createdCustomerDraft()
    when: 'invoking POST'
      def res = post('/drafts2', postData)
    then:
      with(res) {
        status == 201
        location.path == "/drafts2/${CUSTOMER_DRAFT_ID}"
      }
      res.getEntity(Map.class).drafts == createdCustomerDraft()
  }

  String createCustomerDraftPostData() {
    '''\
      {
        "drafts": {
          "entityType": "customers",
          "entityName": "Jan Nowak",
          "owner": "test",
          "status": "DRAFT",
          "data": {
          }
        }
      }
    '''.stripIndent()
  }

  def createdCustomerDraft() {
    [
        id: CUSTOMER_DRAFT_ID,
        entityType: 'customers',
        entityId: 0,
        entityName: 'Jan Nowak',
        owner: 'test',
        status: 'DRAFT',
        data: [
            name: 'Jan Nowak'
        ]
    ]
  }

  @SuppressWarnings("all")
  DraftRepository wiredDraftRepositoryStub() {
    DraftRepository repo = Stub(DraftRepository.class)
    draftResource.draftRepository = repo
  }

  ClientResponse post(String path, String json) {
    reqTo(path, json).post(ClientResponse.class)
  }

  PartialRequestBuilder reqTo(String path, String jsonData) {
    testRule.client().resource(path)
        .entity(jsonData, MediaType.APPLICATION_JSON_TYPE)
        .accept(MediaType.APPLICATION_JSON_TYPE)
  }
}
