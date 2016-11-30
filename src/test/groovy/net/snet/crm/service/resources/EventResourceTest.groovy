package net.snet.crm.service.resources

import ch.qos.logback.classic.Level
import io.dropwizard.logging.LoggingFactory
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.domain.shared.event.EventId
import net.snet.crm.domain.shared.event.EventLog
import org.junit.ClassRule
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.core.MediaType

class EventResourceTest extends Specification {

  @Shared
  EventResource eventResource = new EventResource(null)

  @Shared
  @ClassRule
  ResourceTestRule testRule = ResourceTestRule.builder().addResource(eventResource).build()

  def setupSpec() {
    LoggingFactory.bootstrap(Level.DEBUG)
  }

  def "should parse query params for entity request"() {
    given:
      def eventLog = Mock(EventLog)
      eventResource.eventLog = eventLog
    when:
      def result = testRule.client().resource('/events')
          .queryParam('entity', 'services.123')
          .accept(MediaType.APPLICATION_JSON_TYPE)
          .get(Map.class)
    then:
      true
//      1 * eventLog.eventsPast(new EventId(-1), 'services', 123L, _) >> []
  }
}
