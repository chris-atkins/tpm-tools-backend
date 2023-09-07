package com.poorknight.tpmtoolsbackend.domain.projectplan.entity;

import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class ProjectPlanPatchTemplate {

	private Long id;

	private String title;

	private List<RowPatchTemplate> rowList;
}
