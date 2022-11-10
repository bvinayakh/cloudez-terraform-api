package com.cez.terraform.api.v1.request;

import org.springframework.util.Base64Utils;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestObject
{
  @JsonProperty("encoded-script")
  private String script;

  @JsonProperty("description")
  private String description;

  @JsonProperty("owner")
  private String owner;

  @JsonProperty("id")
  private String id;

  public RequestObject()
  {
    // TODO Auto-generated constructor stub
  }

  public String getDesription()
  {
    return this.description;
  }

  public String getScript()
  {
    return new String(Base64Utils.decodeFromString(this.script));
  }

  public String getEncodedScript()
  {
    return this.script;
  }

  public String getOwner()
  {
    return owner;
  }

  public String getId()
  {
    return id;
  }
}
