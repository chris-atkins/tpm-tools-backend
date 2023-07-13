package com.poorknight.tpmtoolsbackend.domain.projectplan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectPlanService  {

	@Autowired
	private ProjectPlanRepository projectPlanRepository;

	public ProjectPlan getProjectPlan(Long projectPlanId) {
		return projectPlanRepository.findById(projectPlanId).get();
	}
}
