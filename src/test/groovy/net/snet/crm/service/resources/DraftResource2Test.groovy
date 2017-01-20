package net.snet.crm.service.resources
import ch.qos.logback.classic.Level
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.PartialRequestBuilder
import io.dropwizard.logging.LoggingFactory
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.domain.model.network.NetworkRepository
import net.snet.crm.domain.model.network.NetworkService
import net.snet.crm.domain.model.draft.DraftRepository
import org.junit.ClassRule
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.core.MediaType

class DraftResource2Test extends Specification {

  private static final DRAFTS = 'drafts2'
  private static final long CUSTOMER_DRAFT_ID = 10L
  private static final long AGREEMENT_DRAFT_ID = 10L

  @Shared
  DraftResource2 draftResource = new DraftResource2(null, null, null, null, null, null);

  @Shared
  @ClassRule
  ResourceTestRule testRule = ResourceTestRule.builder().addResource(draftResource).build()

  def setupSpec() {
    LoggingFactory.bootstrap(Level.DEBUG)
  }

  @Ignore
  def 'should disable dhcp when deleting dhcp draft'() {
    given:
      def original = [id: 1, entityType: 'services', entitySpate: '1', entityId: 10, entityName: '',
                      status: 'DRAFT', owner: 'test',
                      data: [auth_type: '1', auth_a: '11', auth_b: '3']]
    and:
      def draftRepository = wiredDraftRepositoryStub()
      draftRepository.get(_) >> original
      def networkRepository = wiredNetworRepositoryMock()
      def networkService = wiredNetworServiceMock()
    when:
      def res = reqTo("/${DRAFTS}/1234", '').delete(ClientResponse.class)
    then:
      with(res) {
        status == 204
      }
    and:
      1 * networkRepository.disableDhcp(11, 3)
  }

  @Ignore
  def 'should enable dhcp when updating new dhcp draft'() {
    given:
      def original = [id: 1, entityType: 'services', entitySpate: '1', entityId: 10, entityName: '',
                      status: 'DRAFT', owner: 'test', data: [:]]
      def current = [id: 1, entityType: 'services', entitySpate: '1', entityId: 10, entityName: '',
                     status: 'DRAFT', owner: 'test',
                     data: [auth_type: '1', auth_a: '11', auth_b: '2']] // auth_type == 1 => DHCP
    and:
      def draftRepository = wiredDraftRepositoryStub()
      draftRepository.get(_) >>> [original, current]
      def networkRepository = wiredNetworRepositoryMock()
      def networkService = wiredNetworServiceMock()
    when:
      def res = reqTo("/${DRAFTS}/1234", '{"drafts":{}}').put(ClientResponse.class)
    then:
      with(res) {
        status == 200
      }
      1 * networkRepository.bindDhcp(10, 11, 2)
  }

  def 'should create new customer draft resource'() {
    given:
      def postData = postCustomerDraft()
    and:
      def draftRepository = wiredDraftRepositoryStub()
      draftRepository.create(_) >> CUSTOMER_DRAFT_ID
      draftRepository.get(CUSTOMER_DRAFT_ID) >> createdCustomerDraft()
    when:
      def res = post("/${DRAFTS}", postData)
    then:
      with(res) {
        status == 201
        location.path.endsWith("${DRAFTS}/${CUSTOMER_DRAFT_ID}")
      }
      res.getEntity(Map.class).drafts == createdCustomerDraft()
  }

  String postCustomerDraft() {
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
        entityId: 8,
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

  @SuppressWarnings("all")
  NetworkRepository wiredNetworRepositoryMock() {
    NetworkRepository repo = Mock(NetworkRepository.class)
    draftResource.networkRepository = repo
  }

  @SuppressWarnings("all")
  NetworkService wiredNetworServiceMock() {
    NetworkService service = Mock(NetworkService.class)
    draftResource.networkService = service
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
