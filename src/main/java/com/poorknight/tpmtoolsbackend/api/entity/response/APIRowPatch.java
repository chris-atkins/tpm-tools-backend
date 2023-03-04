package com.poorknight.tpmtoolsbackend.api.entity.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@Getter
@EqualsAndHashCode
@ToString
public class APIRowPatch {

	private final String title;

	@JsonCreator
	public APIRowPatch(@JsonProperty("title") String title) {
		this.title = title;
	}
}
