package com.poorknight.tpmtoolsbackend.domain.row.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class RowPatchTemplate {
	private Long id;
	private String title;
	List<RowPatchTemplateTask> taskList;
}
