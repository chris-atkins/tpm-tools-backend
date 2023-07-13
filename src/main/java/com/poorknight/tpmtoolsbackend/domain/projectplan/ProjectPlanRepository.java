package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.row.Row;
import org.springframework.data.repository.CrudRepository;

/*package private*/ interface ProjectPlanRepository extends CrudRepository<ProjectPlan, Long> {
	//empty for spring magic
}