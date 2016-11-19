package net.snet.crm.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;

import java.io.IOException;
import java.util.Map;

public class JsonUtil {
  private static ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  public static void configure(ObjectMapper mapper) {
    JsonUtil.mapper = mapper;
  }

  public static String jsonOf(Data data) {
    try {
      return mapper.writeValueAsString(data.asMap());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static Data dataOf(String json) {
    try {
      return MapData.of((Map<String, Object>) mapper.readValue(json, Map.class));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
