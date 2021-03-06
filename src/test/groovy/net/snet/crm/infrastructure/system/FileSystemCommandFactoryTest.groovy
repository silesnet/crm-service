package net.snet.crm.infrastructure.system

import spock.lang.IgnoreIf
import spock.lang.Specification

class FileSystemCommandFactoryTest extends Specification {
  def "should create system command factory from existing folder"() {
    expect:
      factory()
  }

  @IgnoreIf({ os.linux })
  def "should instantiate system command from file name"() {
    expect:
      factory().systemCommand('kickPppoeUser', '-u', 'user', '-d', 'device').run()
      factory().systemCommand('configureDhcpPort', '-s', 'switch', '-p', 'port', '-v', 'value').run()
      factory().systemCommand('sendEmail', '-a', 'address', '-s', 'subject', '-m', 'message').run()
  }

  @IgnoreIf({ os.linux })
  def "should capture commands output"() {
    given:
    def command = factory().systemCommand('fetchDhcpWirelessConnection', '-m', 'master', '-a', 'MAC')
    when:
    command.run()
    then:
    !command.output().isEmpty()
  }

  FileSystemCommandFactory factory() {
    new FileSystemCommandFactory(new File('src/test/resources/commands'))
  }

}
