package com.cez.api.v1.terraform;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeploymentsRepository extends JpaRepository<Deployments, String>
{
}
