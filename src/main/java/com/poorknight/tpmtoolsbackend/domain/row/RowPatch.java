package com.poorknight.tpmtoolsbackend.domain.row;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RowPatch {
	private Long id;
	private String title;
}
