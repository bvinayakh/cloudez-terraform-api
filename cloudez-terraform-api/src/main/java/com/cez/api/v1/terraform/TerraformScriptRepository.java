package com.cez.api.v1.terraform;

import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface TerraformScriptRepository extends JpaRepository<TerraformScript, String>
{
}
