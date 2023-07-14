package com.poorknight.tpmtoolsbackend.domain.row.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class RowPatchTemplateTask {
	Long id;
	Integer size;
	Integer position;
}
