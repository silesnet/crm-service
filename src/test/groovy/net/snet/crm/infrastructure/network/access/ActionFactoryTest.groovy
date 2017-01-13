package net.snet.crm.infrastructure.network.access

import net.snet.crm.domain.model.network.NetworkRepository
import net.snet.crm.domain.model.network.NetworkService
import net.snet.crm.infrastructure.network.access.action.*
import spock.lang.Specification
import spock.lang.Unroll

import static net.snet.crm.infrastructure.network.access.Transitions.*

class ActionFactoryTest extends Specification {

  @Unroll
  def "should return action from transaction"() {
    setup:
    def networkRepository = Stub(NetworkRepository)
    def networkService = Stub(NetworkService)
    def factory = new ActionFactory(networkRepository, networkService)
    expect:
    factory.actionOf(transiton).class == clazz
    where:
    transiton                  || clazz
    NoneToPppoe                || EnablePppoe
    NoneToDhcp                 || EnableDhcp
    NoneToDhcpWireless         || EnableDhcpWireless
    PppoeToPppoe               || UpdatePppoe
    PppoeToDhcp                || DisablePppoeEnableDhcp
    PppoeToDhcpWireless        || DisablePppoeEnableDhcpWireless
    PppoeToStatic              || DisablePppoe
    PppoeToNone                || DisablePppoe
    DhcpToPppoe                || DisableDhcpEnablePppoe
    DhcpToDhcp                 || UpdateDhcp
    DhcpToDhcpWireless         || DisableDhcpEnableDhcpWireless
    DhcpToStatic               || DisableDhcp
    DhcpToNone                 || DisableDhcp
    DhcpWirelessToPppoe        || DisableDhcpWirelessEnablePppoe
    DhcpWirelessToDhcp         || DisableDhcpWirelessEnableDhcp
    DhcpWirelessToDhcpWireless || UpdateDhcpWireless
    DhcpWirelessToStatic       || DisableDhcpWireless
    DhcpWirelessToNone         || DisableDhcpWireless
    StaticToPppoe              || EnablePppoe
    StaticToDhcp               || EnableDhcp
    StaticToDhcpWireless       || EnableDhcpWireless
    NULL                       || NoAction
  }

}
