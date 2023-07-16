package com.poorknight.tpmtoolsbackend.api.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplateTask;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class APIRowPatchTask {

	Long id;
	Integer size;
	Integer position;


	@JsonCreator
	public APIRowPatchTask(@JsonProperty(value ="id", required=true) Long id, @JsonProperty(value = "size", required = false) Integer size, @JsonProperty(value = "position", required = false) Integer position) {
		this.id = id;
		this.size = size;
		this.position = position;
	}

	public RowPatchTemplateTask toDomainObject() {
		return new RowPatchTemplateTask(this.id, this.size, this.position);
	}
}
