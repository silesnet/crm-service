package net.snet.network.shared;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;

@Provider
@Consumes("application/vnd.api+json")
public class JsonApiMessageBodyReader implements MessageBodyReader<JsonApiBody> {
  private final ObjectMapper mapper = new ObjectMapper();

  public JsonApiMessageBodyReader() {
  }

  @Override
  public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
    return aClass == JsonApiBody.class;
  }

  @Override
  public JsonApiBody readFrom(Class<JsonApiBody> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
    final Map<String, Object> map = mapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
    final List<JsonApiResource> resources = new ArrayList<>();
    final Object data = map.get("data");
    if (data instanceof List) {
      resources.addAll(
          ((List<Map<String, Object>>) data).stream().map(this::jsonApiResource).collect(Collectors.toList())
      );
    } else if (data instanceof Map) {
      resources.add(jsonApiResource((Map<String, Object>) data)
      );
    }
    return new JsonApiBody(resources);
  }

  private JsonApiResource jsonApiResource(Map<String, Object> resource) {
    return new JsonApiResource(
        Optional.ofNullable(resource.get("id")).map(Object::toString).orElse(null),
        resource.get("type").toString(),
        kebabCaseKeysToCamelCaseKeys((Map<String, Object>) resource.get("attributes")));
  }

  private Map<String, Object> kebabCaseKeysToCamelCaseKeys(Map<String, Object> map) {
    final Map<String, Object> result = new HashMap<>();
    map.forEach((key, value) -> {
      final String camelCaseKey = LOWER_HYPHEN.to(LOWER_CAMEL, key);
      result.put(camelCaseKey, value);
    });
    return result;
  }

}
