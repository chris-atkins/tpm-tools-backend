package com.poorknight.tpmtoolsbackend.api.entity.response;

import com.google.common.collect.ImmutableList;
import com.poorknight.tpmtoolsbackend.domain.row.Row;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class APIRow {

	private final Long id;
	private final Long projectPlanId;
	private final String title;
	private final List<APITask> tasks;

	public static APIRow fromDomainObject(Row row) {
		List<APITask> taskList = transformToAPITasks(row.getTaskList());
		return new APIRow(row.getId(), row.getProjectPlanId(), row.getTitle(), taskList);
	}

	public Row toDomainObject() {
		List<Task> taskList = transformToDomainTasks();
		return new Row(this.id, this.projectPlanId, this.title, taskList);
	}

	private static List<APITask> transformToAPITasks(List<Task> taskList) {
		if (taskList == null) {
			return ImmutableList.<APITask>builder().build();
		}

		List<APITask> apiTasksList = new ArrayList<>(taskList.size());
		for (Task task : taskList) {
			apiTasksList.add(APITask.fromDomainObject(task));
		}
		return apiTasksList;
	}

	private List<Task> transformToDomainTasks() {
		if (this.tasks == null) {
			return ImmutableList.<Task>builder().build();
		}

		List<Task> taskList = new ArrayList<>(this.tasks.size());
		for (APITask task : this.tasks) {
			taskList.add(task.toDomainObject());
		}
		return taskList;
	}
}
