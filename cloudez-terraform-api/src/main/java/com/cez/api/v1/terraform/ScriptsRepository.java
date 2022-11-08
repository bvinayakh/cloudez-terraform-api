package com.cez.api.v1.terraform;

import org.springframework.stereotype.Repository;

@Repository
// public interface ScriptsRepository extends JpaRepository<Scripts, String>
public interface ScriptsRepository extends SearchRepository<Scripts, String>
{

}
