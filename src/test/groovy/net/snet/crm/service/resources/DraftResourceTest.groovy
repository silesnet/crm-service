package net.snet.crm.service.resources

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import com.sun.jersey.api.client.ClientResponse
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.bo.Draft
import net.snet.crm.service.dao.CrmRepository
import net.snet.crm.service.dao.DraftDAO
import org.junit.ClassRule
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by admin on 16.9.14.
 */
class DraftResourceTest extends Specification {
	private static CrmRepositoryDelegate CUSTOMER_REPO_DELEGATE = new CrmRepositoryDelegate()
	private static DraftDaoDelegate DRAFT_DAO_DELEGATE = new DraftDaoDelegate()
	private static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
	public static final int SERVICE_ID = 10466601
	public static final int CUSTOMER_ID = 297
	public static final int AGREEMENT_ID = 104666
	public static final int DRAFT_ID = 1234 CrmRepository crmRepository DraftDAO draftDAO;

	@ClassRule
	@Shared
	ResourceTestRule resources = ResourceTestRule.builder()
			.addResource(new DraftResource(DRAFT_DAO_DELEGATE, OBJECT_MAPPER, CUSTOMER_REPO_DELEGATE))
			.build()

	def draftData

	def setup() {
		crmRepository = Mock(CrmRepository)
		CUSTOMER_REPO_DELEGATE.setRepository(crmRepository)
		draftDAO = Mock(DraftDAO)
		DRAFT_DAO_DELEGATE.setDao(draftDAO)
		draftData = null
	}

	def cleanup() {
		CUSTOMER_REPO_DELEGATE.setRepository(null)
	}

	def 'it should insert new service draft when customer and agreement are given'() {
		given: 'service draft creation data'
			def createDraft = '''
{
	"drafts": {
	  "type": "service",
	  "userId": "test",
	  "data": {
			"customer": { "id": 1234 },
			"agreement": { "id": 101234}
		}
	}
}
'''
		when: 'POST draft creation data'
			def response = resources.client().resource('/drafts/new')
					.type('application/json').post(ClientResponse.class, createDraft)
			def draft = response.getEntity(Map.class).drafts
		then: 'response has correct headers'
			response.status == 201
			response.location.toString() ==~ /.*\/drafts\/\d+$/
			response.type.toString().startsWith('application/json')
		and: 'find customer was called'
			1 * crmRepository.findCustomerById(1234L) >> [id: 1234, name: 'Existing Customer']
		and: 'find agreement was called'
			1 * crmRepository.findAgreementById(101234) >> [id: 101234, customer_id: 1234]
		and: 'create service was called'
			1 * crmRepository.insertService(101234) >> [id: 10123401]
		and: 'draft creating and fetching calls were made'
			1 * draftDAO.insertDraft({ Draft d ->
				d.id == 0
				d.type == 'service'
				d.userId == 'test'
				d.data != null
				draftData = d.data
			} as Draft) >> 2345
			1 * draftDAO.findDraftById(2345) >> { new Draft(2345, 'service', 'test', draftData as String, 'DRAFT') }
		and: 'response body contains new draft'
			draft.id == 2345
			draft.type == 'service'
			draft.user_id == 'test'
		  draft.status == 'DRAFT'
			draft.data != null
			def draftDataMap = resources.objectMapper.readValue(draft.data as String, Map.class)
			draftDataMap.customer.id == 1234
			draftDataMap.agreement.id == 101234
			draftDataMap.service.id == 10123401
	}

	def 'it should insert new service draft when customer is given'() {
		given: 'service draft creation data'
			def createDraft = '''
{
	"drafts": {
	  "type": "service",
	  "userId": "test",
	  "data": {
			"customer": { "id": 1234 },
			"agreement": { "country": "CZ" }
		}
	}
}
'''
		when: 'POST draft creation data'
			def response = resources.client().resource('/drafts/new')
					.type('application/json').post(ClientResponse.class, createDraft)
			def draft = response.getEntity(Map.class).drafts
		then: 'response has correct headers'
			response.status == 201
			response.location.toString() ==~ /.*\/drafts\/\d+$/
			response.type.toString().startsWith('application/json')
		and: 'find customer was called'
			1 * crmRepository.findCustomerById(1234L) >> [id: 1234, name: 'Existing Customer']
		and: 'create agreement was called'
			1 * crmRepository.insertAgreement(1234, 'CZ') >> [id: 101234, customer_id: 1234]
		and: 'create service was called'
			1 * crmRepository.insertService(101234) >> [id: 10123401]
		and: 'draft creating and fetching calls were made'
			1 * draftDAO.insertDraft({ Draft d ->
				d.id == 0
				d.type == 'service'
				d.userId == 'test'
				d.data != null
				draftData = d.data
			} as Draft) >> 2345
			1 * draftDAO.findDraftById(2345) >> { new Draft(2345, 'service', 'test', draftData as String, 'DRAFT') }
		and: 'response body contains new draft'
			draft.id == 2345
			draft.type == 'service'
			draft.user_id == 'test'
			draft.status == 'DRAFT'
			draft.data != null
			def draftDataMap = resources.objectMapper.readValue(draft.data as String, Map.class)
			draftDataMap.customer.id == 1234
			draftDataMap.agreement.id == 101234
			draftDataMap.service.id == 10123401
	}

	def 'it should insert new service draft when nor customer nor agreement are given'() {
		given: 'service draft creation data'
			def createDraft = '''
{
	"drafts": {
	  "type": "service",
	  "userId": "test",
	  "data": {
			"customer": { "name": "New Customer", "country": "CZ" },
			"agreement": { "country": "CZ" }
		}
	}
}
'''
		when: 'POST draft creation data'
			def response = resources.client().resource('/drafts/new')
					.type('application/json').post(ClientResponse.class, createDraft)
			def draft = response.getEntity(Map.class).drafts
		then: 'response has correct headers'
			response.status == 201
			response.location.toString() ==~ /.*\/drafts\/\d+$/
			response.type.toString().startsWith('application/json')
		and: 'create customer was called'
			1 * crmRepository.insertCustomer([name: 'New Customer', country: 10]) >> [id: 1234, name: 'New Customer']
		and: 'create agreement was called'
			1 * crmRepository.insertAgreement(1234, 'CZ') >> [id: 101234, customer_id: 1234]
		and: 'create service was called'
			1 * crmRepository.insertService(101234) >> [id: 10123401]
		and: 'draft creating and fetching calls were made'
			1 * draftDAO.insertDraft({ Draft d ->
				d.id == 0
				d.type == 'service'
				d.userId == 'test'
				d.data != null
				draftData = d.data
			} as Draft) >> 2345
			1 * draftDAO.findDraftById(2345) >> { new Draft(2345, 'service', 'test', draftData as String, 'DRAFT') }
		and: 'response body contains new draft'
			draft.id == 2345
			draft.type == 'service'
			draft.user_id == 'test'
			draft.status == 'DRAFT'
			draft.data != null
			def draftDataMap = resources.objectMapper.readValue(draft.data as String, Map.class)
			draftDataMap.customer.id == 1234
			draftDataMap.agreement.id == 101234
			draftDataMap.service.id == 10123401
	}

	def 'it should delete draft with new customer and new agreement'() {
		given: 'service draft of draft customer and draft agreement'
		when: 'service draft is deleted'
			def response = resources.client().resource('/drafts/1234')
					.type('application/json').delete(ClientResponse.class)
		then: 'status is no content'
			response.status == 204
			1 * draftDAO.findDraftById(DRAFT_ID) >> new Draft(DRAFT_ID, 'service', 'test', '{ "customer": { "id": "297" }, "service": { "service_id": "10466601", "contract_no": "104666" } }', 'DRAFT')
			1 * draftDAO.deleteDraftById(DRAFT_ID)
		and: 'service is deleted'
			1 * crmRepository.findServiceById(SERVICE_ID) >> [ status: 'DRAFT']
			1 * crmRepository.deleteService(SERVICE_ID)
		and: 'connection is deleted'
			1 * crmRepository.findConnectionByServiceId(SERVICE_ID) >> [:]
			1 * crmRepository.deleteConnection(SERVICE_ID)
		and: 'customer is deleted'
			1 * crmRepository.findCustomerById(CUSTOMER_ID) >> [ customer_status: 'DRAFT' ]
			1 * crmRepository.deleteCustomer(CUSTOMER_ID)
		and: 'agreement has status available'
			1 * crmRepository.findAgreementById(AGREEMENT_ID) >> [status: 'DRAFT']
			1 * crmRepository.updateAgreementStatus(AGREEMENT_ID, 'AVAILABLE')
	}

	def 'it should delete draft with existing customer and new agreement'() {
		given: 'service draft of existing customer and draft agreement'
		when: 'service draft is deleted'
			def response = resources.client().resource('/drafts/1234')
					.type('application/json').delete(ClientResponse.class)
		then: 'status is no content'
			response.status == 204
			1 * draftDAO.findDraftById(DRAFT_ID) >> new Draft(DRAFT_ID, 'service', 'test', '{ "customer": { "id": "297" }, "service": { "service_id": "10466601", "contract_no": "104666" } }', 'DRAFT')
			1 * draftDAO.deleteDraftById(DRAFT_ID)
		and: 'service is deleted'
			1 * crmRepository.findServiceById(SERVICE_ID) >> [ status: 'DRAFT']
			1 * crmRepository.deleteService(SERVICE_ID)
		and: 'connection is deleted'
			1 * crmRepository.findConnectionByServiceId(SERVICE_ID) >> [:]
			1 * crmRepository.deleteConnection(SERVICE_ID)
		and: 'customer exists'
			1 * crmRepository.findCustomerById(CUSTOMER_ID) >> [ customer_status: 'ACTIVE' ]
			0 * crmRepository.deleteCustomer(CUSTOMER_ID)
		and: 'agreement has status available'
			1 * crmRepository.findAgreementById(AGREEMENT_ID) >> [status: 'DRAFT']
			1 * crmRepository.updateAgreementStatus(AGREEMENT_ID, 'AVAILABLE')
	}

	def 'it should delete draft with existing customer and existing agreement'() {
		given: 'service draft of existing customer and existing agreement'
		when: 'service draft is deleted'
			def response = resources.client().resource('/drafts/1234')
					.type('application/json').delete(ClientResponse.class)
		then: 'status is no content'
			response.status == 204
			1 * draftDAO.findDraftById(DRAFT_ID) >> new Draft(DRAFT_ID, 'service', 'test', '{ "customer": { "id": "297" }, "service": { "service_id": "10466601", "contract_no": "104666" } }', 'DRAFT')
			1 * draftDAO.deleteDraftById(DRAFT_ID)
		and: 'service is deleted'
			1 * crmRepository.findServiceById(SERVICE_ID) >> [ status: 'DRAFT']
			1 * crmRepository.deleteService(SERVICE_ID)
		and: 'connection is deleted'
			1 * crmRepository.findConnectionByServiceId(SERVICE_ID) >> [:]
			1 * crmRepository.deleteConnection(SERVICE_ID)
		and: 'customer exists'
			1 * crmRepository.findCustomerById(CUSTOMER_ID) >> [ customer_status: 'ACTIVE' ]
		and: 'agreement exists'
			1 * crmRepository.findAgreementById(AGREEMENT_ID) >> [status: 'ACTIVE']
			0 * crmRepository.updateAgreementStatus(AGREEMENT_ID, 'AVAILABLE')
	}

	def 'it should update draft status'() {
		given: 'existing draft'
		and: 'service draft update data'
			def updateDraft = '''
{
	"drafts": {
		"status": "ACCEPTED"
	}
}
'''
		when: 'PUT draft update data'
			def response = resources.client().resource('/drafts/new/12')
					.type('application/json').put(ClientResponse.class, updateDraft)
		then: 'response has correct headers'
			response.status == 204
		and: 'find current draft was called'
			1 * draftDAO.findDraftById(12L) >> new Draft(12L, 'service', 'test', '', 'DRAFT')
		and: 'update draft was called'
			1 * draftDAO.updateDraftStatus('ACCEPTED', 12L)
	}

	def 'it should update draft data'() {
		given: 'existing draft'
		and: 'service draft update data'
			def updateDraft = '''
{
	"drafts": {
		"data": "{}"
	}
}
'''
		when: 'PUT draft update data'
			def response = resources.client().resource('/drafts/new/12')
					.type('application/json').put(ClientResponse.class, updateDraft)
		then: 'response has correct headers'
			response.status == 204
		and: 'find current draft was called'
			1 * draftDAO.findDraftById(12L) >> new Draft(12L, 'service', 'test', '', 'DRAFT')
		and: 'update draft was called'
			1 * draftDAO.updateDraftData('{}', 12L)
	}

  @Ignore
	def 'it should import draft data into system'() {
		given: 'accepted draft'
		and: 'draft status update to IMPORTED'
			def updateDraft = '''
{
	"drafts": {
		"status": "IMPORTED"
	}
}
'''
		when: 'PUT draft update data'
			def response = resources.client().resource('/drafts/new/12')
					.type('application/json').put(ClientResponse.class, updateDraft)
		then: 'response has correct headers'
			response.status == 204
		and: 'find current draft was called'
			1 * draftDAO.findDraftById(12L) >> new Draft(12L, 'service', 'test', sampleDraftData('draft-data-sample.json'), 'ACCEPTED')
		and: 'find customer was called'
			1 * crmRepository.findCustomerById(1442L) >> [:]
		and: 'update customer was called'
			1 * crmRepository.updateCustomer(1442L, _) >> [contract_no: '10, 20']
		and: 'find agreement was called'
			1 * crmRepository.findAgreementById(104667L) >> [id: 104667L, country: 'CZ', customer_id: 1442L , status: 'DRAFT' ]
		and: 'update agreement was called'
			1 * crmRepository.updateAgreementStatus(104667L, 'ACTIVE')
		and: 'set customer agreements was called'
			1 * crmRepository.setCustomerAgreements(1442L, '10, 20, 4667')
		and: 'find service was called'
			1 * crmRepository.findServiceById(10466701L) >> [:]
		and: 'update service was called'
			1 * crmRepository.updateService(10466701L, _)
		and: 'update draft status was called'
			1 * draftDAO.updateDraftStatus('IMPORTED', 12L)
	}

  String sampleDraftData(String resource) {
		new File(Resources.getResource(resource).file).getText('UTF-8')
	}

	static class CrmRepositoryDelegate implements CrmRepository {
		@Delegate
		CrmRepository repository;

		def setRepository(CrmRepository repository) {
			this.repository = repository
		}
	}

	static class DraftDaoDelegate implements DraftDAO {
		@Delegate
		DraftDAO dao

		def setDao(DraftDAO dao) {
			this.dao = dao
		}
	}

}
