package com.poorknight.tpmtoolsbackend.api.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class APIProjectPlanPatchRow {

    private final Long id;
    private final List<APIProjectPlanPatchTask> tasks;
}
