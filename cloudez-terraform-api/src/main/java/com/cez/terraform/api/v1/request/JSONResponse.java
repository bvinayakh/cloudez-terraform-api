package com.cez.terraform.api.v1.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JSONResponse
{
  public String getResponse(JsonNode result) throws JsonProcessingException
  {
    JSONOM mapper = new JSONOM();
    ObjectNode parentNode = mapper.createObjectNode();

    ObjectNode serviceNode = mapper.createObjectNode();
    serviceNode.put("service", "terraform");
    serviceNode.put("version", "1.0");
    parentNode.with("response").set("metadata", serviceNode);

    ObjectNode contentNode = mapper.createObjectNode();
    contentNode.putPOJO("content", result);
    parentNode.with("response").set("output", contentNode);

    return mapper.writeValueAsString(parentNode);
  }
}
