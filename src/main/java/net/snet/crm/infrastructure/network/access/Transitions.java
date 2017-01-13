package net.snet.crm.infrastructure.network.access;

public enum  Transitions
{
  NULL,
  NoneToPppoe,
  NoneToDhcp,
  NoneToDhcpWireless,
  PppoeToPppoe,
  PppoeToDhcp,
  PppoeToDhcpWireless,
  PppoeToStatic,
  PppoeToNone,
  DhcpToPppoe,
  DhcpToDhcp,
  DhcpToDhcpWireless,
  DhcpToStatic,
  DhcpToNone,
  DhcpWirelessToPppoe,
  DhcpWirelessToDhcp,
  DhcpWirelessToDhcpWireless,
  DhcpWirelessToStatic,
  DhcpWirelessToNone,
  StaticToPppoe,
  StaticToDhcp,
  StaticToDhcpWireless
}
