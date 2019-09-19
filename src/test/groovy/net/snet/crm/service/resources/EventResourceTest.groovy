package net.snet.crm.service.resources

import ch.qos.logback.classic.Level
import io.dropwizard.logging.BootstrapLogging
import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.domain.shared.event.EventConstrain
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
    BootstrapLogging.bootstrap(Level.INFO)
  }

  def "should parse query params for entity request"() {
    given:
      def eventLog = Mock(EventLog)
      eventResource.eventLog = eventLog
    when:
      testRule.client().target('/events')
          .queryParam('pastEventId', '10')
          .queryParam('event', 'disconnected')
          .queryParam('entity', 'services')
          .queryParam('entityId', '103')
          .request()
          .accept(MediaType.APPLICATION_JSON_TYPE)
          .get(Map.class)
    then:
      1 * eventLog.events(_) >> { EventConstrain constrain ->
        def sql = constrain.sql().replaceAll(' ', '')
        assert sql.contains('id>:id')
        assert sql.contains("event=:event")
        assert sql.contains("entity=:entity")
        assert sql.contains("entity_id=:entity_id")
        assert sql.split(/AND/).size() == 4
        return []
      }
  }
}
