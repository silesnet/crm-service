package net.snet.crm.infrastructure.network.access

import net.snet.crm.domain.model.draft.Draft
import spock.lang.Specification

import static net.snet.crm.infrastructure.network.access.Events.*
import static net.snet.crm.infrastructure.network.access.States.*

class ServiceDraftTest extends Specification {
  def "should return default state and event"() {
    def draft = new Draft(draftData())
    def serviceDraft = new ServiceDraft(draft)
    expect:
    serviceDraft.state() == None
    serviceDraft.event() == Created
  }

  def "should return Pppoe state and event"() {
    def data = draftData()
    data.data.auth_type = 2
    def serviceDraft = new ServiceDraft(new Draft(data))
    expect:
    serviceDraft.state() == Pppoe
    serviceDraft.event() == PppoeConfigured
  }

  def "should return Dhcp state and event"() {
    def data = draftData()
    data.data.auth_type = 1
    data.data.product_channel = 'lan'
    def serviceDraft = new ServiceDraft(new Draft(data))
    expect:
    serviceDraft.state() == Dhcp
    serviceDraft.event() == DhcpConfigured
  }

  def "should return DhcpWireless state and event"() {
    def data = draftData()
    data.data.auth_type = 1
    data.data.product_channel = 'wireless'
    def serviceDraft = new ServiceDraft(new Draft(data))
    expect:
    serviceDraft.state() == DhcpWireless
    serviceDraft.event() == DhcpWirelessConfigured
  }

  def "should return Static state and event"() {
    def data = draftData()
    data.data.config = 2
    def serviceDraft = new ServiceDraft(new Draft(data))
    expect:
    serviceDraft.state() == Static
    serviceDraft.event() == StaticConfigured
  }

  def "should service id"() {
    def serviceDraft = new ServiceDraft(new Draft(draftData()))
    expect:
    serviceDraft.serviceId() == 20236901
  }

  def draftData() {
    [
        id          : 229,
        entity_type : 'services',
        entity_spate: '202369',
        entity_id   : 20236901,
        entity_name : '',
        status      : 'DRAFT',
        owner       : 'test',
        data        : [
            :
        ]
    ]
  }
}
