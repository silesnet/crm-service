package net.snet.crm.service.resources

import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.PartialRequestBuilder
import io.dropwizard.testing.junit.ResourceTestRule
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.core.MediaType


class DraftResource2Test extends Specification {

  @Shared
  DraftResource2 draftResource = new DraftResource2();

  @Shared
  @ClassRule
  ResourceTestRule testRule = ResourceTestRule.builder().addResource(draftResource).build()

  def 'should create new resource'() {
    given: 'customer POST draft data'
      def postData = '''\
          {
            "drafts": {
              "entityType": "customers",
              "entityName": "Jan Nowak",
              "owner": "test",
              "status": "DRAFT",
              "data": {
              }
          }
          '''.stripIndent()
    when: 'invoking POST'
      ClientResponse res = reqTo('/drafts2', postData).post(ClientResponse.class)
    then:
      with(res) {
        status == 201
        location.path == '/drafts2/1'
      }
      with(res.getEntity(Map.class).drafts) {
        id == 1
        entityType == 'customers'
        entityId == 0
        entityName == 'Jan Nowak'
        get('owner') == 'test'
        status == 'DRAFT'
        with(data) {
          name == 'Jan Nowak'
        }
      }
  }

  PartialRequestBuilder reqTo(String path, String jsonData) {
    testRule.client().resource(path)
        .entity(jsonData, MediaType.APPLICATION_JSON_TYPE)
        .accept(MediaType.APPLICATION_JSON_TYPE)
  }
}
