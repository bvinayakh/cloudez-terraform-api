package com.cez.api.v1.terraform.async;

import org.springframework.data.jpa.repository.JpaRepository;

@org.springframework.stereotype.Repository
public interface TerraformExecutionRepository extends JpaRepository<TerraformExecutions, String>
{
}
