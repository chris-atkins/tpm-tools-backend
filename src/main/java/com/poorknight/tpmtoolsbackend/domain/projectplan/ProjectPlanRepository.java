package com.poorknight.tpmtoolsbackend.domain.projectplan;

import org.springframework.data.repository.CrudRepository;

/*package private*/ interface ProjectPlanRepository extends CrudRepository<ProjectPlan, Long> {
	//empty for spring magic
}