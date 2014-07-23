package net.snet.crm.service

import spock.lang.Specification

import javax.ws.rs.core.UriBuilder

class UriBuilderTest extends Specification {
  def 'it should transform absolute URI to absolute URI'() {
    given: 'absolute URI'
      URI base = URI.create('https://host:8443/customers/1234/agreements?country=CZ')
      UriBuilder builder = UriBuilder.fromUri(base)
    expect:
      builder.replacePath("agreements/201234").replaceQuery("").build()
          .equals(URI.create('https://host:8443/agreements/201234'))
  }
}
