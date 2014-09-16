package net.snet.crm.service.resources

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.jersey.api.client.ClientResponse
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.bo.Draft
import net.snet.crm.service.dao.CrmRepository
import net.snet.crm.service.dao.DraftDAO
import org.junit.ClassRule
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
	public static final int DRAFT_ID = 1234

	CrmRepository crmRepository
	DraftDAO draftDAO;

	@ClassRule
	@Shared
	ResourceTestRule resources = ResourceTestRule.builder()
			.addResource(new DraftResource(DRAFT_DAO_DELEGATE, OBJECT_MAPPER, CUSTOMER_REPO_DELEGATE))
			.build()

	def setup() {
		crmRepository = Mock(CrmRepository)
		CUSTOMER_REPO_DELEGATE.setRepository(crmRepository)
		draftDAO = Mock(DraftDAO)
		DRAFT_DAO_DELEGATE.setDao(draftDAO)
	}

	def cleanup() {
		CUSTOMER_REPO_DELEGATE.setRepository(null)
	}

	def 'it should delete draft with new customer and new agreement'() {
		given: 'service draft of draft customer and draft agreement'
		when: 'service draft is deleted'
			def response = resources.client().resource('/drafts/1234')
					.type('application/json').delete(ClientResponse.class)
		then: 'status is no content'
			response.status == 204
			1 * draftDAO.findDraftById(DRAFT_ID) >> new Draft(DRAFT_ID, 'service', 'test', '{ "customer": { "id": "297", "agreement_id": "104666", "service_id": "10466601", "connection_id": "10466601" } }')
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
			1 * draftDAO.findDraftById(DRAFT_ID) >> new Draft(DRAFT_ID, 'service', 'test', '{ "customer": { "id": "297", "agreement_id": "104666", "service_id": "10466601", "connection_id": "10466601" } }')
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
			1 * draftDAO.findDraftById(DRAFT_ID) >> new Draft(DRAFT_ID, 'service', 'test', '{ "customer": { "id": "297", "agreement_id": "104666", "service_id": "10466601", "connection_id": "10466601" } }')
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
