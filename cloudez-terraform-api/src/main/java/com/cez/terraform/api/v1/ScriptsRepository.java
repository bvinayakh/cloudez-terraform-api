package com.cez.terraform.api.v1;

import org.springframework.stereotype.Repository;

@Repository
// public interface ScriptsRepository extends JpaRepository<Scripts, String>
public interface ScriptsRepository extends SearchRepository<Scripts, String>
{

}
