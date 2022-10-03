package com.cez.api.v1.terraform.request;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JSONOM extends ObjectMapper
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private ObjectMapper mapper = null;

  public JSONOM()
  {
    mapper = new ObjectMapper();
    this.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    // this.setSerializationInclusion(Include.NON_NULL);
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a z");
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    this.setDateFormat(df);
  }

  public JsonNode getJsonNode(String jsonString) throws JsonParseException, JsonMappingException, IOException
  {
    // mapper = new ObjectMapper();
    // mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return mapper.readValue(jsonString, JsonNode.class);
  }

  public JsonNode getJsonNode(List<Object> list) throws JsonParseException, JsonMappingException, IOException
  {
    // mapper = new ObjectMapper();
    // mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
    return mapper.readValue(jsonString, JsonNode.class);
  }

  public String getJsonString(List<?> list) throws JsonProcessingException
  {
    // mapper = new ObjectMapper();
    // mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
  }

  public String getJsonString(String string) throws JsonProcessingException
  {
    // mapper = new ObjectMapper();
    // mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(string);
  }

  @Override
  public ObjectNode createObjectNode()
  {
    // super.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
    // super.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    // super.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    // super.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
    return super.createObjectNode();
  }
}
