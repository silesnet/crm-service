package net.snet.crm.domain.shared.command

import com.google.common.collect.ImmutableMap
import net.snet.crm.domain.shared.data.MapRecord
import spock.lang.Specification

class CommandTest extends Specification {
  def "should build command"() {
    def command = Command.of(MapRecord.of(ImmutableMap.builder()
        .put('id', 1)
        .put('command', 'disconnect')
        .put('entity', 'customers')
        .put('entity_id', 23)
        .put('data', '{}')
        .put('status', 'issued')
        .build()))
    expect:
      command.id().value() == 1
      command.name() == Commands.DISCONNECT
      command.entity() == 'customers'
      command.entityId() == 23
      command.data().asMap().isEmpty()
      command.status() == 'issued'
  }
}
