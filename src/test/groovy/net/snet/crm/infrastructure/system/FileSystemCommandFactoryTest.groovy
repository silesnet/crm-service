package net.snet.crm.infrastructure.system

import spock.lang.Specification

class FileSystemCommandFactoryTest extends Specification {
  def "should create system command factory from existing folder"() {
    expect:
      factory()
  }

  def "should instantiate system command from file name"() {
    expect:
      factory().systemCommand('kickPppoeUser', '-u', 'user', '-d', 'device').run()
      factory().systemCommand('configureDhcpPort', '-s', 'switch', '-p', 'port', '-v', 'value').run()
      factory().systemCommand('sendEmail', '-a', 'address', '-s', 'subject', '-m', 'message').run()
  }

  FileSystemCommandFactory factory() {
    new FileSystemCommandFactory(new File('src/test/resources/commands'))
  }

}
