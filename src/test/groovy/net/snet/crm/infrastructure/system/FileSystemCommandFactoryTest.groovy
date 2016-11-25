package net.snet.crm.infrastructure.system

import spock.lang.Specification

class FileSystemCommandFactoryTest extends Specification {
  def "should create system command factory from existing folder"() {
    expect:
      factory()
  }

  def "should instantiate system command from file name"() {
    expect:
      factory().systemCommand('kickPppoeUser', 'master-1', 'user-1').run()
  }

  FileSystemCommandFactory factory() {
    new FileSystemCommandFactory(new File('src/test/resources/commands'))
  }

}
