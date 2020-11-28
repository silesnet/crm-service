package net.snet.network.shared

import com.fasterxml.jackson.databind.ObjectMapper
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
}
