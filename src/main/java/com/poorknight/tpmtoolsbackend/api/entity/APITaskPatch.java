package com.poorknight.tpmtoolsbackend.api.entity;

import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class APITaskPatch {

	private final Long id;
	private final String title;

	public Task toDomainObject() {
		return new Task(this.id, null, this.title, null, null);
	}
}
