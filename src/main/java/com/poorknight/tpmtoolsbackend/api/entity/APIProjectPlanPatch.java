package com.poorknight.tpmtoolsbackend.api.entity;

import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlanPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.TaskPatchTemplate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class APIProjectPlanPatch {

    private Long id;
    private List<APIProjectPlanPatchRow> rows;

    public ProjectPlanPatchTemplate toDomainObject() {
        return new ProjectPlanPatchTemplate(this.id, null, this.buildRowPatchList(this.rows));
    }

    private List<RowPatchTemplate> buildRowPatchList(List<APIProjectPlanPatchRow> rows) {
        return rows.stream()
                .map((row) -> {
                    return new RowPatchTemplate(row.getId(), null, this.buildTaskPatchList(row.getTasks()));
                }).collect(Collectors.toList());
    }

    private List<TaskPatchTemplate> buildTaskPatchList(List<APIProjectPlanPatchTask> tasks) {
        return tasks.stream()
                .map((task) -> {
                    return TaskPatchTemplate.builder().id(task.getId()).rowId(task.getRowId()).position(task.getPosition()).build();
                }).collect(Collectors.toList());
    }
}
