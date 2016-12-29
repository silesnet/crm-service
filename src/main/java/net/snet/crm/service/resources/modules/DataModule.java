package net.snet.crm.service.resources.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

import java.io.IOException;
import java.util.Map;

public class DataModule extends SimpleModule
{
  public DataModule()
  {
    addSerializer(Data.class, new DataSerializer());
    addDeserializer(Data.class, new DataDeserializer());
  }

  public class DataSerializer extends JsonSerializer<Data>
  {
    @Override
    public void serialize(
        Data data,
        JsonGenerator jgen,
        SerializerProvider provider
    ) throws IOException, JsonProcessingException
    {
      jgen.writeObject(data.asMap());
    }
  }

  public class DataDeserializer extends JsonDeserializer<Data>
  {
    @Override
    public Data deserialize(
        JsonParser jsonParser,
        DeserializationContext ctxt
    ) throws IOException, JsonProcessingException
    {
      final Map map = jsonParser.readValueAs(Map.class);
      return MapData.of((Map<String, Object>) map);
    }
  }

}
