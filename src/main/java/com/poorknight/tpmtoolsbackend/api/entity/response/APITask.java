package com.poorknight.tpmtoolsbackend.api.entity.response;

import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class APITask {

	private final Long id;
	private final Long rowId;
	private final String title;
	private final Integer size;

	public static APITask fromDomainObject(Task task) {
		return new APITask(task.getId(), task.getRowId(), task.getTitle(), task.getSize());
	}

	public Task toDomainObject() {
		return new Task(this.id, this.rowId, this.title, this.size);
	}
}
