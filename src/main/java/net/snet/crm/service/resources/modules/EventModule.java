package net.snet.crm.service.resources.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.snet.crm.domain.shared.event.Event;

import java.io.IOException;

public class EventModule extends SimpleModule {

  public EventModule() {
    addSerializer(Event.class, new EventSerializer());
//    addDeserializer(Event.class, new EventDeserializer());
  }

  public class EventSerializer extends JsonSerializer<Event> {
    @Override
    public void serialize(Event event, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeStringField("type", "events");
      jgen.writeStringField("id", "" + event.id().value());
      jgen.writeObjectField("attributes", event.attributesData().asMap());
      jgen.writeEndObject();
    }
  }

  public class EventDeserializer extends JsonDeserializer<Event> {
    @Override
    public Event deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return null;
    }
  }

}
