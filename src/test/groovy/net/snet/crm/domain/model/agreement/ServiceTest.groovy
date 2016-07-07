package net.snet.crm.domain.model.agreement
import groovy.json.JsonSlurper
import net.snet.crm.domain.model.draft.Draft
import org.joda.time.DateTime
import spock.lang.Specification

class ServiceTest extends Specification {
  public static final String TIMESTAMP_PATTERN = 'YYYY-MM-dd HH:mm:ss'

  def 'should create service from draft'() {
    def draft = new Draft(draftMap(draftJson()))
    def service = new Service(draft)
    expect:
      service.id().value() == 20189501
      with(service.props()) {
        id == 20189501
        customerId == 240
        stamp(periodStart) == '2015-01-13 00:00:00'
        stamp(periodEdn) == null
        productName == 'LanAccess'
        chargingAmount == 436
        connectionDownload == 10
        connectionUpload == 5
        data =~ /"devices"/
      }
  }

  def 'should return database record'() {
    def service = new Service(new Draft(draftMap(draftJson())))
    def record = service.record()
    expect:
      with(record) {
        id == 20189501
        customer_id == 240
        stamp(period_from) == '2015-01-13 00:00:00'
        stamp(period_to) == null
        name == 'LanAccess'
        price == 436
        frequency == 40
        download == 10
        upload == 5
        !is_aggregated
        info == ''
        additionalname == ''
        bps == 'M'
        data =~ /"devices"/
      }
  }

  public static Map<String, Object> draftMap(json) {
    (new JsonSlurper().parseText(draftJson()) as Map).drafts as Map<String, Object>
  }

  public static String draftJson() {
    '''
      {
        "drafts" : {
          "id" : 19,
          "entityType" : "services",
          "entitySpate" : "201895",
          "entityId" : 20189501,
          "entityName" : "",
          "status" : "APPROVED",
          "owner" : "test",
          "data" : {
            "contract_no" : "201895",
            "service_id" : "20189501",
            "product" : "5",
            "product_name": "LanAccess",
            "downlink" : "10",
            "uplink" : "5",
            "price" : "436",
            "ssid" : "435",
            "mac_address" : "",
            "core_router" : null,
            "config" : "1",
            "activate_on" : "13.01.2015",
            "activation_fee" : "",
            "operator" : "16",
            "info_service" : "",
            "location_street" : "Školní 1073",
            "location_descriptive_number" : "",
            "location_orientation_number" : "",
            "location_town" : "Mirošov 1",
            "location_postal_code" : "33843",
            "location_country" : "10",
            "auth_type" : "2",
            "auth_a" : "20189501",
            "auth_b" : "AoDlDMAa",
            "ip" : "",
            "is_ip_public" : false,
            "devices" : [ {
              "name" : "antena",
              "owner" : "silesnet"
            }, {
              "name" : "router",
              "owner" : "customer"
            } ]
          },
          "links" : {
            "customers" : 240,
            "drafts.agreements" : 201895
          }
        }
      }
    '''.stripIndent()
  }

  String stamp(DateTime dateTime) {
    dateTime != null ? dateTime.toString(TIMESTAMP_PATTERN) : null
  }
}
