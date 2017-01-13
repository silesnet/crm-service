package net.snet.crm.infrastructure.network.access

import spock.lang.Specification
import spock.lang.Unroll

import static net.snet.crm.infrastructure.network.access.Events.*
import static net.snet.crm.infrastructure.network.access.States.*
import static net.snet.crm.infrastructure.network.access.Transitions.*

class StateMachineTest extends Specification {
  @Unroll
  def "should return transition of state and event"() {
    def machine = new StateMachine()
    expect:
    machine.transitionOf(state, event) == transition
    where:
    state        | event                  || transition
    None         | Created                || NULL
    None         | PppoeConfigured        || NoneToPppoe
    None         | DhcpConfigured         || NoneToDhcp
    None         | DhcpWirelessConfigured || NoneToDhcpWireless
    None         | StaticConfigured       || NULL
    None         | Deleted                || NULL
    Pppoe        | PppoeConfigured        || PppoeToPppoe
    Pppoe        | DhcpConfigured         || PppoeToDhcp
    Pppoe        | DhcpWirelessConfigured || PppoeToDhcpWireless
    Pppoe        | StaticConfigured       || PppoeToStatic
    Pppoe        | Deleted                || PppoeToNone
    Dhcp         | PppoeConfigured        || DhcpToPppoe
    Dhcp         | DhcpConfigured         || DhcpToDhcp
    Dhcp         | DhcpWirelessConfigured || DhcpToDhcpWireless
    Dhcp         | StaticConfigured       || DhcpToStatic
    Dhcp         | Deleted                || DhcpToNone
    DhcpWireless | PppoeConfigured        || DhcpWirelessToPppoe
    DhcpWireless | DhcpConfigured         || DhcpWirelessToDhcp
    DhcpWireless | DhcpWirelessConfigured || DhcpWirelessToDhcpWireless
    DhcpWireless | StaticConfigured       || DhcpWirelessToStatic
    DhcpWireless | Deleted                || DhcpWirelessToNone
    Static       | PppoeConfigured        || StaticToPppoe
    Static       | DhcpConfigured         || StaticToDhcp
    Static       | DhcpWirelessConfigured || StaticToDhcpWireless
    Static       | StaticConfigured       || NULL
    Static       | Deleted                || NULL
  }
}
