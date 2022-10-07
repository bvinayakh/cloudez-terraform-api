package com.cez.api.v1.terraform.request;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

public class JSONRequest
{
  public static final Logger logger = LoggerFactory.getLogger(JSONRequest.class);

  private JsonNode rootNode = null;
  private JSONOM mapper = null;

  public JSONRequest(String request)
  {
    try
    {
      mapper = new JSONOM();
      this.rootNode = new JSONOM().getJsonNode(request);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public String getService()
  {
    return rootNode.get("request").get("metadata").get("service").asText();
  }

  public String getVersion()
  {
    return rootNode.get("request").get("metadata").get("version").asText();
  }

  public String getProvider()
  {
    return rootNode.get("request").get("metadata").get("provider").asText();
  }

  public String getAccount()
  {
    return rootNode.get("request").get("metadata").get("account").asText();
  }

  public String getDeploymentName()
  {
    return rootNode.get("request").get("parameters").get("name").asText();
  }

  public String getDeploymentDescription()
  {
    return rootNode.get("request").get("parameters").get("description").asText();
  }

  public String getDeploymentOwner()
  {
    return rootNode.get("request").get("parameters").get("owner").asText();
  }

  public String getRegion()
  {
    return rootNode.get("request").get("metadata").get("region").asText();
  }

  public Object getContent()
  {
    RequestObject request = null;
    try
    {
      JsonNode contentNode = rootNode.get("request").get("parameters").get("content");
      request = mapper.readValue(contentNode.toString(), new TypeReference<RequestObject>() {});
    }
    catch (JsonParseException e)
    {
      e.printStackTrace();
    }
    catch (JsonMappingException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return request;
  }

}
