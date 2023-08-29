package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.BaseUnitTestWithDatabase;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProjectPlanServiceTest extends BaseUnitTestWithDatabase {

	@Autowired
	private ProjectPlanService service;

	@BeforeEach
	void setUp() {
		deleteAllTasksAndRowsAndProjectPlans();
	}

	@Test
	void canGetSavedProjectPlan() {
		Long projectPlanId = createProjectPlanWithSQLOnly("project plan title");
		Long row1Id = createRowWithSQLOnly(projectPlanId, "row 1 title");
		Long row2Id = createRowWithSQLOnly(projectPlanId, "row 2 title");
		createTaskWithSQLOnly(row1Id, "row 1 task 1");
		createTaskWithSQLOnly(row2Id, "row 2 task 1");
		createTaskWithSQLOnly(row2Id, "row 2 task 2");

		ProjectPlan projectPlan = service.getProjectPlan(projectPlanId);

		assertThat(projectPlan.getId()).isEqualTo(projectPlanId);
		assertThat(projectPlan.getTitle()).isEqualTo("project plan title");
		List<Row> rows = projectPlan.getRowList();
		assertThat(rows.size()).isEqualTo(2);

		Row row1 = rows.get(0);
		assertThat(row1.getProjectPlanId()).isEqualTo(projectPlanId);
		assertThat(row1.getTitle()).isEqualTo("row 1 title");
		List<Task> row1Tasks = row1.getTaskList();
		assertThat(row1Tasks.size()).isEqualTo(1);
		assertThat(row1Tasks.get(0).getRowId()).isEqualTo(row1Id);
		assertThat(row1Tasks.get(0).getTitle()).isEqualTo("row 1 task 1");

		Row row2 = rows.get(1);
		assertThat(row2.getProjectPlanId()).isEqualTo(projectPlanId);
		assertThat(row2.getTitle()).isEqualTo("row 2 title");
		List<Task> row2Tasks = row2.getTaskList();
		assertThat(row2Tasks.size()).isEqualTo(2);
		assertThat(row2Tasks.get(0).getRowId()).isEqualTo(row2Id);
		assertThat(row2Tasks.get(0).getTitle()).isEqualTo("row 2 task 1");
		assertThat(row2Tasks.get(1).getRowId()).isEqualTo(row2Id);
		assertThat(row2Tasks.get(1).getTitle()).isEqualTo("row 2 task 2");
	}

	@Test
	void getProjectPlanThrowsExceptionIfNoneIsFoundMatchingId() {
		ProjectPlanNotFoundException e = assertThrows(ProjectPlanNotFoundException.class,
				() -> service.getProjectPlan(-1L));
		assertThat(e.getMessage()).contains("No project plan found for the given id: -1");
	}
}