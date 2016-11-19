package net.snet.crm.infrastructure.persistence.jdbi

import com.google.common.io.Resources
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.Handle
import spock.lang.Shared
import spock.lang.Specification

public class BaseDbiSpecification extends Specification {
  @Shared
  DBI dbi = new DBI("jdbc:h2:mem:commandQueueTest")
  @Shared
  Handle handle

  def setup() {
    handle = dbi.open()
    handle.execute(Resources.getResource('db/h2-crm-tables.sql').text)
    init()
  }

  def cleanup() {
    handle.execute(Resources.getResource('db/h2-drop-crm-tables.sql').text)
    handle.close()
  }

  def init() {}
}
