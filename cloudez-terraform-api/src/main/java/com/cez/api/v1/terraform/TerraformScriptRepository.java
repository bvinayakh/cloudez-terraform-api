package com.cez.api.v1.terraform;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface TerraformScriptRepository extends JpaRepository<TerraformScript, String>
{
  List searchby(String text, int limit, String... fields);
}
