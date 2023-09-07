package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlanPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.TaskPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import org.springframework.stereotype.Service;

import java.util.*;

/**
	In charge of making sure any proposed changes to a row or project plan does not result in 2 tasks (or rows) sharing the same space.
 */
@Service
public class ProjectConsistencyValidator {

	public void validateProjectPlanChangeSetThrowingExceptions(ProjectPlan projectPlan, ProjectPlanPatchTemplate projectPlanPatchTemplate) {
		if (projectPlanPatchTemplate.getRowList() == null) {
			return;
		}
		try {
			validateProjectPlanChangeSetForRows(projectPlan.getRowList(), projectPlanPatchTemplate);
		} catch (RowUpdateConsistencyException e) {
			throw new ProjectPlanUpdateConsistencyException(e.getMessage());
		}
	}

	public void validateRowChangeSetThrowingExceptions(Row row, RowPatchTemplate rowPatchTemplate) {
		validateProjectPlanChangeSetForRows(
				List.of(row),
				new ProjectPlanPatchTemplate(row.getProjectPlanId(), null, List.of(rowPatchTemplate)));
	}

	private void validateProjectPlanChangeSetForRows(List<Row> rows, ProjectPlanPatchTemplate projectPlanPatchTemplate) {
		Map<Long, Task> originalTasks = buildOriginalTaskMap(rows);
		Map<Long, TaskPatchTemplate> changedTasks = buildChangedTaskMap(projectPlanPatchTemplate.getRowList());
		Map<Long, TaskPatchTemplate> combinedTasks = buildCombinedTasks(originalTasks, changedTasks);

		validateThatNoOverlapsExistThrowingExceptions(rows, combinedTasks);
	}

	private static void validateThatNoOverlapsExistThrowingExceptions(List<Row> rows, Map<Long, TaskPatchTemplate> combinedTasks) {
		Map<Long, Set<Integer>> occupiedSpaces = new HashMap<>();
		for (Row row : rows) {
			occupiedSpaces.put(row.getId(), new HashSet<>());
		}
		for (TaskPatchTemplate taskPatchTemplate : combinedTasks.values()) {
			for (int i = 1; i <= taskPatchTemplate.getSize(); i++) {
				Integer location = i + taskPatchTemplate.getPosition();
				boolean wasBlank = occupiedSpaces.get(taskPatchTemplate.getRowId()).add(location);
				if (!wasBlank) {
					throw new RowUpdateConsistencyException("The proposed change results in more than one task occupying the same space.");
				}
			}
		}
	}

	private Map<Long, Task> buildOriginalTaskMap(List<Row> rows) {
		Map<Long, Task> taskMap = new HashMap<>();
		for (Row row : rows) {
			for (Task task : row.getTaskList()) {
				taskMap.put(task.getId(), task);
			}
		}
		return taskMap;
	}

	private Map<Long, TaskPatchTemplate> buildChangedTaskMap(List<RowPatchTemplate> rows) {
		Map<Long, TaskPatchTemplate> taskMap = new HashMap<>();
		for (RowPatchTemplate row : rows) {
			for (TaskPatchTemplate task : row.getTaskList()) {
				taskMap.put(task.getId(), task);
			}
		}
		return taskMap;
	}

	private Map<Long, TaskPatchTemplate> buildCombinedTasks(Map<Long, Task> originalTasks, Map<Long, TaskPatchTemplate> changedTasks) {
		Map<Long, TaskPatchTemplate> combinedTaskMap = new HashMap<>();
		for (Task task : originalTasks.values()) {
			combinedTaskMap.put(task.getId(), TaskPatchTemplate.builder()
					.id(task.getId())
					.rowId(task.getRowId())
					.size(task.getSize())
					.position(task.getPosition()).build());
		}

		for (TaskPatchTemplate task : changedTasks.values()) {
			Task original = originalTasks.get(task.getId());
			if (original == null) {
				throw new RowUpdateConsistencyException("The patch request refers to a task ID that does not exist.");
			}
			TaskPatchTemplate newTask = hydrateTaskPatchTemplate(original, task);
			combinedTaskMap.put(task.getId(), newTask);
		}
		return combinedTaskMap;
	}

	private TaskPatchTemplate hydrateTaskPatchTemplate(Task original, TaskPatchTemplate task) {
		return TaskPatchTemplate.builder()
				.id(task.getId())
				.rowId(task.getRowId() != null ? task.getRowId() : original.getRowId())
				.size(task.getSize() != null ? task.getSize() : original.getSize())
				.position(task.getPosition() != null ? task.getPosition() : original.getPosition())
				.build();
	}

	public static class RowUpdateConsistencyException extends RuntimeException {
		public RowUpdateConsistencyException(String message) {
			super(message);
		}
	}

	public static class ProjectPlanUpdateConsistencyException extends RuntimeException {
		public ProjectPlanUpdateConsistencyException(String message) {
			super(message);
		}
	}
}
