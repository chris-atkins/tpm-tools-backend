package com.poorknight.tpmtoolsbackend.domain.projectplan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ProjectPlanService  {

	@Autowired
	private ProjectPlanRepository projectPlanRepository;

	public ProjectPlan getProjectPlan(Long projectPlanId) {
		Optional<ProjectPlan> projectPlan = projectPlanRepository.findById(projectPlanId);
		if (projectPlan.isEmpty()) {
			throw new ProjectPlanNotFoundException("No project plan found for the given id: " + projectPlanId);
		}
		return projectPlan.get();
	}

	public static class ProjectPlanNotFoundException extends RuntimeException {

		public ProjectPlanNotFoundException(String message) {
			super(message);
		}
	}
}

