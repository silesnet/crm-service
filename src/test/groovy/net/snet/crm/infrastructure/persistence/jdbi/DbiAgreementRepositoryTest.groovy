package net.snet.crm.infrastructure.persistence.jdbi
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import net.snet.crm.domain.model.agreement.AgreementRepository
import net.snet.crm.domain.model.agreement.Service
import net.snet.crm.domain.model.agreement.ServiceTest
import net.snet.crm.domain.model.draft.Draft
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

class DbiAgreementRepositoryTest extends Specification {
  @Shared DBI dbi = new DBI("jdbc:h2:mem:customerRepositoryTest")
  @Shared Handle handle

  @Shared
  AgreementRepository repo

  def setup() {
    repo = new DbiAgreementRepository(dbi, new ObjectMapper())
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
  }

  def 'should add service'() {
    given:
      def service = new Service(new Draft(ServiceTest.draftMap(ServiceTest.draftJson())))
      when:
      def added = repo.addService(service)
    then:
      def row = handle.createQuery('SELECT * FROM services WHERE id=:id').bind('id', added.id().value()).asList().first()
      with(row) {
        id == service.id().value()
        period_from.time == service.props().periodStart.millis
        period_to == service.props().periodEnd
        name == service.props().productName
        price == service.props().chargingAmount
        download == service.props().connectionDownload
        upload == service.props().connectionUpload
      }
  }

  def cleanup() {
    handle.execute(Resources.getResource('db/h2-drop-crm-tables.sql').text)
    handle.close()
    repo = null
  }

}
