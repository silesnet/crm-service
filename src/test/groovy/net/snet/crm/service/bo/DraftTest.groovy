package net.snet.crm.service.bo

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification


class DraftTest extends Specification {
  def "it should fetch service id"() {
    given: "draft with service id"
      def draft = new Draft('service', 'test', '{"customer":{"service_id": "1234" }}')
      def om = new ObjectMapper()
    when: "fetching service id"
      long serviceId = draft.serviceId(om);
    then:
      serviceId == 1234
  }
}
