package com.cez.api.v1.terraform.request;

import org.springframework.util.Base64Utils;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestObject
{
  @JsonProperty("encoded-script")
  private String script;

  @JsonProperty("resource")
  private String resource;

  public RequestObject()
  {
    // TODO Auto-generated constructor stub
  }

  public String getScript()
  {
    return new String(Base64Utils.decodeFromString(this.script));
  }

  public String getEncodedScript()
  {
    return this.script;
  }

  public String getResource()
  {
    return resource;
  }

}
