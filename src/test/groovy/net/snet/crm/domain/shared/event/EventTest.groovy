package net.snet.crm.domain.shared.event

import spock.lang.Specification

import static com.google.common.collect.ImmutableMap.builder
import static net.snet.crm.domain.shared.data.MapRecord.of

class EventTest extends Specification {
  def "should create Event"() {
    expect:
      new Event(of(builder().put("id", 1).build()))
  }
}
