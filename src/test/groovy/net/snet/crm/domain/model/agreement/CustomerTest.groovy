package net.snet.crm.domain.model.agreement

import groovy.json.JsonSlurper
import net.snet.crm.domain.model.draft.Draft
import spock.lang.Specification

class CustomerTest extends Specification {

  def 'should instantiate from draft'() {
    def customer = new Customer(new Draft(draftMap(draftJson())))
    expect:
      with(customer.props()) {
        id == 12345
        name == 'Novy Zakaznik'
        nameExtra == 'X'
        addressStreet == 'Dlouha 568/96'
        addressTown == 'Chomutov'
        addressPostalCode == '12345'
        addressCountryId == 10
        contactName == 'Y'
        contactEmail == 'novy.zakaznink@seznam.cz'
        contactPhone == '739 123 456'
        publicId == '99912346'
        taxId == 'CZ99912346'
        otherInfo == 'Dalsi podrobne informace k zakaznikovi.'
      }
  }

  def 'should return record'() {
    def customer = new Customer(new Draft(draftMap(draftJson())))
    def record = customer.record()
    expect:
      with(record) {
        id == 12345
        symbol == ''
      }
  }

  public static Map<String, Object> draftMap(String json) {
    (new JsonSlurper().parseText(json) as Map).drafts as Map<String, Object>
  }

  public static String draftJson() {
    '''\
    {
      "drafts" : {
        "id" : 19,
        "entityType" : "customers",
        "entitySpate" : "",
        "entityId" : 12345,
        "entityName" : "Novy",
        "status" : "APPROVED",
        "owner" : "test",
        "data" : {
          "customer_type" : "1",
          "name" : "Novy",
          "surname" : "Zakaznik",
          "supplementary_name" : "X",
          "public_id" : "99912346",
          "dic" : "CZ99912346",
          "representative" : "Y",
          "email" : "novy.zakaznink@seznam.cz",
          "phone" : "739 123 456",
          "contact_name" : "Oskar Wilde",
          "info" : "Dalsi podrobne informace k zakaznikovi.",
          "street" : "Dlouha",
          "descriptive_number" : "568",
          "orientation_number" : "96",
          "town" : "Chomutov",
          "postal_code" : "12345",
          "country" : "10"
        }
      }
    }
    '''.stripIndent()
  }
}
