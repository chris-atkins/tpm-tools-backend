package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.BaseUnitTestWithDatabase;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService.InvalidProjectPlanUpdateTemplateException;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlanPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.RowService;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.TaskPatchTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.Objects;

import static com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectConsistencyValidator.ProjectPlanUpdateConsistencyException;
import static com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService.ProjectPlanNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@SpyBean(classes = {RowService.class, ProjectConsistencyValidator.class, ProjectPlanRepository.class, TaskService.class})
class ProjectPlanServiceTest extends BaseUnitTestWithDatabase {

	@Autowired
	private ProjectPlanService service;

	@Autowired
	private RowService rowService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private ProjectConsistencyValidator projectConsistencyValidator;

	@Autowired
	private ProjectPlanRepository projectPlanRepository;

	@BeforeEach
	void setUp() {
		deleteAllTasksAndRowsAndProjectPlans();
		Mockito.reset(rowService);
		Mockito.reset(taskService);
		Mockito.reset(projectConsistencyValidator);
		Mockito.reset(projectPlanRepository);
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

	@Test
	void updateChecksValidatorPassingDomainObjectAndPatchRequestAndThrowsAnyExceptionFromIt() {
		Long projectPlanId = createProjectPlanWithSQLOnly("project plan title");
		Long row1Id = createRowWithSQLOnly(projectPlanId, "row 1 title");
		Long row2Id = createRowWithSQLOnly(projectPlanId, "row 2 title");
		createTaskWithSQLOnly(row1Id, "row 1 task 1");
		createTaskWithSQLOnly(row2Id, "row 2 task 1");
		createTaskWithSQLOnly(row2Id, "row 2 task 2");

		ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(projectPlanId, "", List.of());
		Mockito.doThrow(new ProjectPlanUpdateConsistencyException("hi")).when(projectConsistencyValidator).validateProjectPlanChangeSetThrowingExceptions(any(), eq(patchTemplate));

		ProjectPlanUpdateConsistencyException e = assertThrows(ProjectPlanUpdateConsistencyException.class,
				() -> service.updateProjectPlan(patchTemplate));

		assertThat(e.getMessage()).contains("hi");
	}

	@Test
	void updateCanMakeASingleUpdateCallForATaskInTheTemplate() {
		Long projectPlanId = createProjectPlanWithSQLOnly("project plan title");
		Long row1Id = createRowWithSQLOnly(projectPlanId, "row 1 title");
		Long row2Id = createRowWithSQLOnly(projectPlanId, "row 2 title");
		createTaskWithSQLOnly(row1Id, "row 1 task 1", 1, 1);
		Long taskId = createTaskWithSQLOnly(row2Id, "row 2 task 1", 1, 2);
		createTaskWithSQLOnly(row2Id, "row 2 task 2", 1, 3);

		ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(projectPlanId, null,
														List.of(new RowPatchTemplate(row2Id, null,
															List.of(TaskPatchTemplate.builder().id(taskId).position(5).build()))));

		ProjectPlan projectPlan = service.updateProjectPlan(patchTemplate);

		List<Row> updatedRows = projectPlan.getRowList();
		assertThat(updatedRows.size()).isEqualTo(2);
		List<Task> taskList2 = updatedRows.get(1).getTaskList();
		assertThat(taskList2.size()).isEqualTo(2);
		boolean tested = false;
		for (Task task : taskList2) {
			if (Objects.equals(task.getId(), taskId)) {
				assertThat(task.getPosition()).isEqualTo(5);
				assertThat(task.getSize()).isEqualTo(1);
				tested = true;
			}
		}
		assertThat(tested).isTrue();
 	}
	@Test
	void updateCanMoveATaskBetweenRows() {
		Long projectPlanId = createProjectPlanWithSQLOnly("project plan title");
		Long row1Id = createRowWithSQLOnly(projectPlanId, "row 1 title");
		Long row2Id = createRowWithSQLOnly(projectPlanId, "row 2 title");
		createTaskWithSQLOnly(row1Id, "row 1 task 1", 1, 1);
		Long taskId = createTaskWithSQLOnly(row2Id, "row 2 task 1", 1, 2);
		createTaskWithSQLOnly(row2Id, "row 2 task 2", 1, 3);

		ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(projectPlanId, null,
														List.of(new RowPatchTemplate(row1Id, null,
															List.of(TaskPatchTemplate.builder()
																	.id(taskId)
																	.rowId(row1Id)
																	.position(5).build()))));

		ProjectPlan projectPlan = service.updateProjectPlan(patchTemplate);

		List<Row> updatedRows = projectPlan.getRowList();
		assertThat(updatedRows.size()).isEqualTo(2);

		assertThat(updatedRows.get(0).getId()).isEqualTo(row1Id);
		List<Task> taskList1 = updatedRows.get(0).getTaskList();

		assertThat(updatedRows.get(1).getId()).isEqualTo(row2Id);
		List<Task> taskList2 = updatedRows.get(1).getTaskList();

		assertThat(taskList1.size()).isEqualTo(2);
		assertThat(taskList2.size()).isEqualTo(1);

		boolean tested = false;
		for (Task task : taskList1) {
			if (Objects.equals(task.getId(), taskId)) {
				assertThat(task.getPosition()).isEqualTo(5);
				assertThat(task.getSize()).isEqualTo(1);
				tested = true;
			}
		}
		assertThat(tested).isTrue();
 	}

	@Test
	void updateCanMoveATaskBetweenRowsAndChangePositionsOfOthers() {
		Long projectPlanId = createProjectPlanWithSQLOnly("project plan title");
		Long row1Id = createRowWithSQLOnly(projectPlanId, "row 1 title");
		Long row2Id = createRowWithSQLOnly(projectPlanId, "row 2 title");
		Long taskId2 = createTaskWithSQLOnly(row1Id, "row 1 task 1", 1, 1);
		Long taskId1 = createTaskWithSQLOnly(row2Id, "row 2 task 1", 1, 2);
		createTaskWithSQLOnly(row2Id, "row 2 task 2", 1, 3);

		ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(projectPlanId, null,
														List.of(new RowPatchTemplate(row1Id, null,
															List.of(TaskPatchTemplate.builder()
																		.id(taskId1)
																		.rowId(row1Id)
																		.position(1).build(),
																	TaskPatchTemplate.builder()
																			.id(taskId2)
																			.rowId(row1Id)
																			.position(2).build()))));

		ProjectPlan projectPlan = service.updateProjectPlan(patchTemplate);

		List<Row> updatedRows = projectPlan.getRowList();
		assertThat(updatedRows.size()).isEqualTo(2);

		assertThat(updatedRows.get(0).getId()).isEqualTo(row1Id);
		List<Task> taskList1 = updatedRows.get(0).getTaskList();

		assertThat(updatedRows.get(1).getId()).isEqualTo(row2Id);
		List<Task> taskList2 = updatedRows.get(1).getTaskList();

		assertThat(taskList1.size()).isEqualTo(2);
		assertThat(taskList2.size()).isEqualTo(1);

		boolean tested1 = false;
		boolean tested2 = false;
		for (Task task : taskList1) {
			if (Objects.equals(task.getId(), taskId1)) {
				assertThat(task.getPosition()).isEqualTo(1);
				assertThat(task.getSize()).isEqualTo(1);
				tested1 = true;
			}
			if (Objects.equals(task.getId(), taskId2)) {
				assertThat(task.getPosition()).isEqualTo(2);
				assertThat(task.getSize()).isEqualTo(1);
				tested2 = true;
			}
		}
		assertThat(tested1).isTrue();
		assertThat(tested2).isTrue();
 	}

	@Test
	void updateCanChangeJustTheProjectPlanTitle() {

		Long projectPlanId = createProjectPlanWithSQLOnly("project plan title");
		Long row1Id = createRowWithSQLOnly(projectPlanId, "row 1 title");
		createTaskWithSQLOnly(row1Id, "row 1 task 1", 1, 1);

		ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(projectPlanId, "new title", null);

		ProjectPlan projectPlan = service.updateProjectPlan(patchTemplate);

		assertThat(projectPlan.getTitle()).isEqualTo("new title");

		List<Row> updatedRows = projectPlan.getRowList();
		assertThat(updatedRows.size()).isEqualTo(1);

		assertThat(updatedRows.get(0).getId()).isEqualTo(row1Id);
		List<Task> taskList1 = updatedRows.get(0).getTaskList();
		assertThat(taskList1.size()).isEqualTo(1);
	}

	@Test
	void updateWithRowsMustAlsoContainTasks() {
		ProjectPlanPatchTemplate patchTemplateWithNullTasks = new ProjectPlanPatchTemplate(1L, null,
				List.of(new RowPatchTemplate(2L, null, null)));

		InvalidProjectPlanUpdateTemplateException e = assertThrows(InvalidProjectPlanUpdateTemplateException.class,
				() -> service.updateProjectPlan(patchTemplateWithNullTasks));

		assertThat(e.getMessage()).contains("If a row is in a project plan update template, then that row must have one or more tasks to update within it.");


		ProjectPlanPatchTemplate patchTemplateWithEmptyTasks = new ProjectPlanPatchTemplate(1L, null,
				List.of(new RowPatchTemplate(2L, null, List.of())));

		InvalidProjectPlanUpdateTemplateException ex = assertThrows(InvalidProjectPlanUpdateTemplateException.class,
				() -> service.updateProjectPlan(patchTemplateWithNullTasks));

		assertThat(ex.getMessage()).contains("If a row is in a project plan update template, then that row must have one or more tasks to update within it.");
	}

	@Test
	void doesNotAllowTaskSizeToBeSetOnAnUpdateTemplate() {
		ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(1L, null,
				List.of(new RowPatchTemplate(2L, null,
						List.of(TaskPatchTemplate.builder()
										.id(3L)
										.rowId(2L)
										.position(1).build(),
								TaskPatchTemplate.builder()
										.id(4L)
										.rowId(2L)
										.size(2).build()))));

		InvalidProjectPlanUpdateTemplateException e = assertThrows(InvalidProjectPlanUpdateTemplateException.class,
				() -> service.updateProjectPlan(patchTemplate));

		assertThat(e.getMessage()).contains("Size is not a valid field to change for a task using a project plan update.  Please use a row update for changing a task size.");
	}

	@Test
	void doesNotAllowATemplateWithNoNameOrRows() {
		ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(1L, null, null);

		InvalidProjectPlanUpdateTemplateException e = assertThrows(InvalidProjectPlanUpdateTemplateException.class,
				() -> service.updateProjectPlan(patchTemplate));


		ProjectPlanPatchTemplate patchTemplateWithEmptyList = new ProjectPlanPatchTemplate(1L, null, List.of());

		InvalidProjectPlanUpdateTemplateException ex = assertThrows(InvalidProjectPlanUpdateTemplateException.class,
				() -> service.updateProjectPlan(patchTemplateWithEmptyList));

		assertThat(ex.getMessage()).contains("A project plan update template must have at least one of rows or title specified.");
	}
}