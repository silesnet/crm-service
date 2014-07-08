package net.snet.crm.service

import io.dropwizard.testing.junit.ResourceTestRule
import net.snet.crm.service.resources.CustomerResource
import org.junit.ClassRule
import org.skife.jdbi.v2.DBI
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by sikorric on 2014-07-08.
 */
class CustomerResourceTest extends Specification {
  @Shared
  DBI JDBI = new DBI('jdbc:h2:mem:')

  @ClassRule
  @Shared
  ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(new CustomerResource(JDBI))
      .build()

  def 'it should run'() {
    given:

    when:
      println resources.client().resource('/customers').get(String.class)
    then:
      false
  }

}
