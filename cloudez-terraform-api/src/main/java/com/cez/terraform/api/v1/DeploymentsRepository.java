package com.cez.terraform.api.v1;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentsRepository extends JpaRepository<Deployments, String>
{
}
