package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlanPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.TaskPatchTemplate;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProjectPlanService  {

	@Autowired
	private ProjectPlanRepository projectPlanRepository;

	@Autowired
	private TaskService taskService;

	@Autowired
	private ProjectConsistencyValidator projectConsistencyValidator;

	@Autowired
	EntityManager entityManager;

	public ProjectPlan getProjectPlan(Long projectPlanId) {
		Optional<ProjectPlan> projectPlan = projectPlanRepository.findById(projectPlanId);
		if (projectPlan.isEmpty()) {
			throw new ProjectPlanNotFoundException("No project plan found for the given id: " + projectPlanId);
		}
		return projectPlan.get();
	}

	public ProjectPlan updateProjectPlan(ProjectPlanPatchTemplate patchTemplate) {
		validateUpdateTemplateThrowingException(patchTemplate);

		performProjectPlanUpdate(patchTemplate);
		entityManager.clear();
		return this.getProjectPlan(patchTemplate.getId());
	}

	private void validateUpdateTemplateThrowingException(ProjectPlanPatchTemplate patchTemplate) {

		if (patchTemplate.getTitle() == null && (patchTemplate.getRowList() == null || patchTemplate.getRowList().size() == 0)) {
			throw new InvalidProjectPlanUpdateTemplateException("A project plan update template must have at least one of rows or title specified.");
		}

		if (patchTemplate.getRowList() == null) {
			return;
		}

		for (RowPatchTemplate rowUpdateTemplate : patchTemplate.getRowList()) {
			if (rowUpdateTemplate.getTaskList() == null || rowUpdateTemplate.getTaskList().size() == 0) {
				throw new InvalidProjectPlanUpdateTemplateException("If a row is in a project plan update template, then that row must have one or more tasks to update within it.");
			}
			for (TaskPatchTemplate taskUpdateTemplate : rowUpdateTemplate.getTaskList()) {
				if (taskUpdateTemplate.getSize() != null) {
					throw new InvalidProjectPlanUpdateTemplateException("Size is not a valid field to change for a task using a project plan update.  Please use a row update for changing a task size.");
				}
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	private void performProjectPlanUpdate(ProjectPlanPatchTemplate patchTemplate) {
		ProjectPlan projectPlan = this.getProjectPlan(patchTemplate.getId());
		projectConsistencyValidator.validateProjectPlanChangeSetThrowingExceptions(projectPlan, patchTemplate);

		if (patchTemplate.getTitle() != null) {
			projectPlan.setTitle(patchTemplate.getTitle());
			projectPlanRepository.save(projectPlan);
		}

		if (patchTemplate.getRowList() == null) {
			return;
		}

		for(RowPatchTemplate rowPatchTemplate : patchTemplate.getRowList()) {
			this.updateTasksInRow(rowPatchTemplate);
		}
	}

	private void updateTasksInRow(RowPatchTemplate rowPatchTemplate) {
		for (TaskPatchTemplate taskPatchTemplate : rowPatchTemplate.getTaskList()) {
			Task taskBeingUpdated = taskService.findTaskWithId(taskPatchTemplate.getId());

			if (taskPatchTemplate.getPosition() != null) {
				taskBeingUpdated.setPosition(taskPatchTemplate.getPosition());
			}

			if (taskPatchTemplate.getRowId() != null) {
				taskBeingUpdated.setRowId(taskPatchTemplate.getRowId());
			}

			taskService.patchTask(taskBeingUpdated);
		}
	}

	public static class ProjectPlanNotFoundException extends RuntimeException {

		public ProjectPlanNotFoundException(String message) {
			super(message);
		}
	}

	public static class InvalidProjectPlanUpdateTemplateException extends RuntimeException {

		public InvalidProjectPlanUpdateTemplateException(String message) {
			super(message);
		}
	}
}

