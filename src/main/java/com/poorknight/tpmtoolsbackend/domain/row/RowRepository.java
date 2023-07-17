package com.poorknight.tpmtoolsbackend.domain.row;

import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/*package private*/ interface RowRepository extends CrudRepository<Row, Long> {

	List<Row> findByProjectPlanId(Long projectPlanId);
}