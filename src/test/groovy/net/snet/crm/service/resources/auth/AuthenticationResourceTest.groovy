package net.snet.crm.service.resources.auth

import net.snet.crm.domain.shared.auth.User
import net.snet.crm.domain.shared.auth.UserRepository
import net.snet.crm.service.auth.SessionId
import spock.lang.Specification

class AuthenticationResourceTest extends Specification {
  def 'should convert optional user to user session'() {
    def users = Mock(UserRepository)
    def user = new User("login", "Name", "CZ", new String[0])
    users.fetchByLogin(_) >> Optional.of(user)
    expect:
      users.fetchByLogin()
          .map({ new UserSession(it) })
          .orElseThrow({ -> throw new RuntimeException("fail") }) == new UserSession(user)
  }

  def 'should throw on empty user'() {
    def users = Mock(UserRepository)
    users.fetchByLogin(_) >> Optional.empty()
    when:
    users.fetchByLogin()
        .map({ new UserSession(it) })
        .orElseThrow({ -> throw new RuntimeException("fail") })
    then:
      thrown RuntimeException
  }
}
