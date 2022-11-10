package com.cez.terraform.api.v1.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestObject
{
  private String script;

  @JsonProperty("owner")
  private String owner;

  @JsonProperty("id")
  private String id;

  public RequestObject()
  {
    // TODO Auto-generated constructor stub
  }

//  public String getScript()
//  {
//    return new String(Base64Utils.decodeFromString(this.script));
//  }

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
