package com.poorknight.tpmtoolsbackend.domain.row;

import org.springframework.data.repository.CrudRepository;

/*package private*/ interface RowRepository extends CrudRepository<Row, Long> {
	//empty for spring magic
}