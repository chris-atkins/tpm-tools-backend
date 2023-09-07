package com.poorknight.tpmtoolsbackend.api.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class APIProjectPlanPatchTask {

    private final Long id;
    private final Long rowId;
    private final Integer position;
}
