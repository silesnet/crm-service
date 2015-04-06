package net.snet.crm.domain.model.agreement

import com.google.common.base.Optional
import groovy.json.JsonSlurper
import net.snet.crm.domain.model.draft.Draft
import spock.lang.Specification

class CustomerTest extends Specification {

  def 'should instantiate from draft'() {
    def customer = new Customer(new Draft(draftMap(jsonDraft())))
    expect:
      with(customer.props()) {
        id == 12345
        addressStreet == 'Dlouha 568/96'
        addressTown == 'Chomutov'
        addressPostalCode == '12345'
        addressCountryId == 10
        contactEmail == 'novy.zakaznink@seznam.cz'
        contactPhone == '739 123 456'
        publicId == '99912346'
        otherInfo == 'Dalsi podrobne informace k zakaznikovi.'
      }
  }

  def 'should resolve residential customer name'() {
    def customer = new Customer(new Draft(draftMap(residentialJsonDraft())))
    expect:
      customer.isResidential()
      with(customer.props()) {
        name == 'Novotny Karel, MUDr.'
        nameExtra == ''
        contactName == 'Oskar Wilde'
        taxId == ''
      }
      with(customer.record()) {
        name == 'Novotny Karel, MUDr.'
        supplementary_name == ''
        contact_name == 'Oskar Wilde'
        dic == ''
      }
  }

  def 'should resolve business customer name'() {
    def customer = new Customer(new Draft(draftMap(businessJsonDraft())))
    expect:
      customer.isBusiness()
      with(customer.props()) {
        name == 'Firma Novy'
        nameExtra == 'Karol Representative'
        contactName == 'Oskar Wilde'
        taxId == 'CZ99912346'
      }
      with(customer.record()) {
        name == 'Firma Novy'
        supplementary_name == 'Karol Representative'
        contact_name == 'Oskar Wilde'
        dic == 'CZ99912346'
      }
  }

  def 'should return record'() {
    def customer = new Customer(new Draft(draftMap(jsonDraft())))
    def record = customer.record()
    expect:
      with(record) {
        id == 12345
        symbol == ''
        variable == Optional.absent()
      }
  }

  public static Map<String, Object> draftMap(String json) {
    (new JsonSlurper().parseText(json) as Map).drafts as Map<String, Object>
  }

  public static String jsonDraft() {
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

  public static String businessJsonDraft() {
    '''\
    {
      "drafts" : {
        "id" : 19,
        "entityType" : "customers",
        "entitySpate" : "",
        "entityId" : 12345,
        "entityName" : "Firma Novy",
        "status" : "APPROVED",
        "owner" : "test",
        "data" : {
          "customer_type" : "2",
          "name" : "error, it should be empty",
          "surname" : "error, it should be empty",
          "supplementary_name" : "Firma Novy",
          "public_id" : "99912346",
          "dic" : "CZ99912346",
          "representative" : "Karol Representative",
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
  public static String residentialJsonDraft() {
    '''\
    {
      "drafts" : {
        "id" : 19,
        "entityType" : "customers",
        "entitySpate" : "",
        "entityId" : 12345,
        "entityName" : "Novotny Karel, MUDr. with error",
        "status" : "APPROVED",
        "owner" : "test",
        "data" : {
          "customer_type" : "1",
          "name" : "Karel, MUDr.",
          "surname" : "Novotny",
          "supplementary_name" : "error, it should be empty",
          "public_id" : "99912346",
          "dic" : "error, it should be empty",
          "representative" : "error, it should be empty",
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
