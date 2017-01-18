package net.snet.crm.service.utils

import spock.lang.Specification

import static net.snet.crm.service.utils.JsonUtil.dataOf

class JsonUtilTest extends Specification {
  def "should map null value"() {
    def data = dataOf('{"key": null}')
    println data.asMap()
    expect:
      data.hasPath('key')
      !data.hasValue('key')
      data.optStringOf('key', '') == ''
  }
}
