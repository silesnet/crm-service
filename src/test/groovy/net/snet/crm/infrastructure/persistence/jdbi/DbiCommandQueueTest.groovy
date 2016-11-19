package net.snet.crm.infrastructure.persistence.jdbi

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import net.snet.crm.domain.shared.command.Command
import net.snet.crm.domain.shared.command.CommandQueue
import net.snet.crm.domain.shared.command.Commands
import net.snet.crm.domain.shared.data.Data
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

class DbiCommandQueueTest extends Specification {
  @Shared DBI dbi = new DBI("jdbc:h2:mem:commandQueueTest")
  @Shared Handle handle

  @Shared
  CommandQueue queue

  def setup() {
    queue = new DbiCommandQueue(dbi, new ObjectMapper())
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
  }

  def "should submit command"() {
    when:
      def command = queue.submit(Commands.DISCONNECT, 'customers', '23', Data.EMPTY)
    then:
      command.id().value() == 1
      command.name() == Commands.DISCONNECT
      command.entity() == 'customers'
      command.entityId() == 23
      command.data().asMap().isEmpty()
      command.status() == 'issued'
  }

  def "should return next command to execute"() {
  given:
    def commandA = queue.submit(Commands.DISCONNECT, 'customers', '23', Data.EMPTY)
    def commandB = queue.submit(Commands.RECONNECT, 'customers', '23', Data.EMPTY)
    def commandC = queue.submit(Commands.DISCONNECT, 'customers', '23', Data.EMPTY)
  when:
    def commands = queue.nextOf(Commands.DISCONNECT, 2)
  then:
    commands.size() == 2
    commands[0].id().value() == commandA.id().value()
    commands[1].id().value() == commandC.id().value()
  }

  def "should update status and time stamp on process"() {
    given:
      queue.submit(Commands.DISCONNECT, 'customers', '23', Data.EMPTY)
      def command = queue.nextOf(Commands.DISCONNECT, 1)[0]
    when:
      def startedCommand = queue.process(command.id())
    then:
      queue.nextOf(Commands.DISCONNECT, 1).isEmpty()
      startedCommand.status() == 'started'
  }

  def "should update status and time stamp on complete"() {
    given:
      queue.submit(Commands.DISCONNECT, 'customers', '23', Data.EMPTY)
      def command = queue.nextOf(Commands.DISCONNECT, 1)[0]
    when:
      queue.process(command.id)
      def completed = queue.complete(command.id())
    then:
      queue.nextOf(Commands.DISCONNECT, 1).isEmpty()
      completed.status() == 'completed'
  }

  def "should update status and time stamp on failed"() {
    given:
      queue.submit(Commands.DISCONNECT, 'customers', '23', Data.EMPTY)
      def command = queue.nextOf(Commands.DISCONNECT, 1)[0]
    when:
      queue.process(command.id)
      def completed = queue.fail(command.id())
    then:
      queue.nextOf(Commands.DISCONNECT, 1).isEmpty()
      completed.status() == 'failed'
  }

  def cleanup() {
    handle.execute(Resources.getResource('db/h2-drop-crm-tables.sql').text)
    handle.close()
    queue = null
  }

}
