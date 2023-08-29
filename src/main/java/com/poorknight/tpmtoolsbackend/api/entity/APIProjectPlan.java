package com.poorknight.tpmtoolsbackend.api.entity;

import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
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
public class APIProjectPlan {

    private Long id;
    private String title;
    private List<APIRow> rows;

    public static APIProjectPlan fromDomainObject(ProjectPlan projectPlan) {
        List<APIRow> rows = buildAPIRowsFromDomainObject(projectPlan.getRowList());
        return new APIProjectPlan(projectPlan.getId(), projectPlan.getTitle(), rows);
    }

    private static List<APIRow> buildAPIRowsFromDomainObject(List<Row> rowList) {
        return rowList.stream()
                .map(APIRow::fromDomainObject)
                .collect(Collectors.toList());
    }
}
