package com.poorknight.tpmtoolsbackend.api.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.TaskPatchTemplate;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@EqualsAndHashCode
@ToString
public class APIRowPatch {

	private final String title;
	private final List<APIRowPatchTask> tasks;

	@JsonCreator
	public APIRowPatch(@JsonProperty(value = "title", required = false) String title, @JsonProperty(value = "tasks", required = false) List<APIRowPatchTask> tasks) {
		this.title = title;
		this.tasks = tasks;
	}

	public RowPatchTemplate toDomainObject(Long id) {
		if (this.tasks == null) {
			return new RowPatchTemplate(id, this.title, null);
		}

		List<TaskPatchTemplate> taskList = new ArrayList<>();
		for (APIRowPatchTask task : this.tasks) {
			taskList.add(task.toDomainObject());
		}
		return new RowPatchTemplate(id, this.title, taskList);
	}
}
