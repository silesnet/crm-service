package net.snet.crm.infrastructure.network.access

import net.snet.crm.domain.shared.data.MapData
import spock.lang.Specification

import static net.snet.crm.infrastructure.network.access.Events.*
import static net.snet.crm.infrastructure.network.access.States.*

class AccessTest extends Specification {
  def "should return default state and event"() {
    def data = draftData()
    def access = new Access(MapData.of(data))
    expect:
    access.state() == None
    access.event() == Deleted
  }

  def "should return Pppoe state and event"() {
    def data = draftData()
    data.data.auth_type = 2
    def access = new Access(MapData.of(data))
    expect:
    access.state() == Pppoe
    access.event() == PppoeConfigured
  }

  def "should return Dhcp state and event"() {
    def data = draftData()
    data.data.auth_type = 1
    data.data.product_channel = 'lan'
    def access = new Access(MapData.of(data))
    expect:
    access.state() == Dhcp
    access.event() == DhcpConfigured
  }

  def "should return DhcpWireless state and event"() {
    def data = draftData()
    data.data.auth_type = 1
    data.data.product_channel = 'wireless'
    def access = new Access(MapData.of(data))
    expect:
    access.state() == DhcpWireless
    access.event() == DhcpWirelessConfigured
  }

  def "should return Static state and event"() {
    def data = draftData()
    data.data.config = 2
    def access = new Access(MapData.of(data))
    expect:
    access.state() == Static
    access.event() == StaticConfigured
  }

  def draftData() {
    [
        id          : 229,
        entityType : 'services',
        entitySpate: '202369',
        entityId   : 20236901,
        entityName : '',
        status      : 'DRAFT',
        owner       : 'test',
        data        : [
            product_name: 'LANaccess'
        ]
    ]
  }
}
