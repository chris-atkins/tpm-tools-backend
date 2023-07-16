package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplateTask;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProjectConsistencyValidator {

	public void validateRowChangeSetThrowingExceptions(Row row, RowPatchTemplate rowPatchTemplate) {
		Set<Integer> occupiedSpaces = new HashSet<>();
		Set<Long> previouslyProcessedTaskIds = new HashSet<>();

		RowPatchTemplate hydratedTemplate = buildHydratedRowPatchTemplate(rowPatchTemplate, row);
		for (RowPatchTemplateTask taskUpdate : hydratedTemplate.getTaskList()) {
			previouslyProcessedTaskIds.add(taskUpdate.getId());
			for (int i = 0; i < taskUpdate.getSize(); i++) {
				occupiedSpaces.add(taskUpdate.getPosition() + i);
			}
		}

		for (Task task : row.getTaskList()) {
			boolean isNewTask = previouslyProcessedTaskIds.add(task.getId());
			if (!isNewTask) continue;

			for(int i = 0; i < task.getSize(); i++) {
				boolean wasFreeSPace = occupiedSpaces.add(task.getPosition() + i);
				if (!wasFreeSPace) {
					throw new RowUpdateConsistencyException("The proposed change results in more than one task occupying the same space.");
				}
			}
		}
	}

	private RowPatchTemplate buildHydratedRowPatchTemplate(RowPatchTemplate rowPatchTemplate, Row row) {
		RowPatchTemplate hydratedTemplate = new RowPatchTemplate(rowPatchTemplate.getId(), rowPatchTemplate.getTitle(), new ArrayList<>(rowPatchTemplate.getTaskList().size()));
		Map<Long, Task> taskMap = new HashMap<>();
		for (Task task : row.getTaskList()) {
			taskMap.put(task.getId(), task);
		}

		for (RowPatchTemplateTask taskTemplate : rowPatchTemplate.getTaskList()) {
			Task taskForHydrating = taskMap.get(taskTemplate.getId());
			RowPatchTemplateTask hydratedTaskTemplate = buildHydratedTaskPatchTemplate(taskTemplate, taskForHydrating);
			hydratedTemplate.getTaskList().add(hydratedTaskTemplate);
		}
		return hydratedTemplate;
	}

	private RowPatchTemplateTask buildHydratedTaskPatchTemplate(RowPatchTemplateTask taskTemplate, Task taskForHydrating) {
		Integer size = taskTemplate.getSize() == null ? taskForHydrating.getSize() : taskTemplate.getSize();
		Integer position = taskTemplate.getPosition() == null ? taskForHydrating.getPosition() : taskTemplate.getPosition();
		return new RowPatchTemplateTask(taskTemplate.getId(), size, position);
	}


	public static class RowUpdateConsistencyException extends RuntimeException {
		public RowUpdateConsistencyException(String message) {
			super(message);
		}
	}
}
