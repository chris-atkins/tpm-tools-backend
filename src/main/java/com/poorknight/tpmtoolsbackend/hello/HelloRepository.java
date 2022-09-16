package com.poorknight.tpmtoolsbackend.hello;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
/*package private*/ interface HelloRepository extends CrudRepository<HelloMessage, Long> {
	//empty for spring magic
}
