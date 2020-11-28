package net.snet.network.shared

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.google.common.collect.Sets
import spock.lang.Shared
import spock.lang.Specification

import javax.ws.rs.ext.MessageBodyReader

class JsonApiMessageBodyReaderTest extends Specification {
  @Shared
  MessageBodyReader<JsonApiBody> reader = new JsonApiMessageBodyReader(new ObjectMapper())

  def 'it should only read JsonApiBody.class'() {
    expect:
    reader.isReadable(JsonApiBody.class, null, null, null)
    !reader.isReadable(Object.class, null, null, null)
  }

  def 'it should read JsonApiBody from request body'() {
    given:
    def requestBody = '''
    {
      "data": {
        "id": 1,
        "type": "nodes",
        "attributes": {
          "name": "name-1",
          "foo": null
        }
      }
    }
    '''
    when:
    JsonApiBody body = reader.readFrom(null, null, null, null, null, new ByteArrayInputStream(requestBody.getBytes()))
    then:
    body.hasSingleResource()
    body.resource().id() == '1'
    body.resource().type() == 'nodes'
    body.resource().attributes() == [name: 'name-1', foo: null]
  }

  def 'it should read to network Node'() {
    expect:
    new ObjectMapper().convertValue([id: 1, name: 'name-1'], net.snet.network.Node)
  }

  def 'it should write network Node to JSON'() {
    expect:
    new ObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategy.KebabCaseStrategy() {
      private static final Set<Class> BOOLEAN_CLASSES = Sets.newHashSet(boolean.class, Boolean.class)

      @Override
      String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
        String propertyName = defaultName
        if (BOOLEAN_CLASSES.contains(method.getRawReturnType())) {
          if (('is' + defaultName).toLowerCase() == method.getName().toLowerCase()) {
            propertyName = method.getName()
          }
        }
        return super.nameForGetterMethod(config, method, propertyName)
      }
    }).writeValueAsString(net.snet.network.Node.builder().isWireless(true).build()).contains('is-wireless')
  }
}
