package com.poorknight.tpmtoolsbackend.domain.tasks.entity;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
public class TaskPatchTemplate {
	Long id;
	Long rowId;
	Integer size;
	Integer position;
}
