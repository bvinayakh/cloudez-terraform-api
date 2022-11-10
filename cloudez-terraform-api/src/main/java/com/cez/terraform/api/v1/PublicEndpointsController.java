package com.cez.terraform.api.v1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("terraform/public")
public class PublicEndpointsController
{
  @GetMapping("/ping")
  String ping()
  {
    return ("cloudez-terraform-api");
  }
}