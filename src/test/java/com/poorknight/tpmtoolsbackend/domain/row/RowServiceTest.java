package com.poorknight.tpmtoolsbackend.domain.row;

import com.poorknight.tpmtoolsbackend.domain.BaseUnitTestWithDatabase;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectConsistencyValidator;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplateTask;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;

@SpyBean(classes = {TaskRepository.class, ProjectConsistencyValidator.class})
class RowServiceTest extends BaseUnitTestWithDatabase {

	@Autowired
	private RowService rowService;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ProjectConsistencyValidator projectConsistencyValidator;

	private Long projectPlanId;

	@BeforeEach
	public void setUp() {
		this.deleteAllTasksAndRowsAndProjectPlans();
		projectPlanId = createProjectPlanWithSQLOnly("new plan");
		Mockito.reset(taskRepository);
	}

	@Test
	void canSaveNewRowWithTitleAndNoTasks() throws Exception {
		this.rowService.saveNewRow(new Row(projectPlanId, "new row title", new ArrayList<>()));

		int rowCount = findCountOfRows();
		assertThat(rowCount).isEqualTo(1);

		boolean found = canFindRowWithTitle("new row title");
		assertThat(found).isTrue();
	}

	@Test
	void savingRowReturnsId() {
		Row row = this.rowService.saveNewRow(new Row(projectPlanId,"new row title", new ArrayList<>()));
		assertThat(row.getId()).isNotNull();
	}

	@Test
	void cannotSaveNewRowWithIdSpecified() {
		try {
			this.rowService.saveNewRow(new Row(1L, projectPlanId,"title", new ArrayList<>()));
			fail("Expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).contains("New Row cannot be saved with an id.  This is auto-assigned by the DB.  Maybe you would like to use an update operation.");
		}
	}

	@Test
	void cannotSaveNewRowWithTaskList() {
		try {
			List<Task> taskList = ImmutableList.<Task>builder().add(new Task(5L, "tilte", 1, 5)).build();
			Row newRow = new Row(null, projectPlanId, "title", taskList);
			this.rowService.saveNewRow(newRow);
			fail("Expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).contains("New Row cannot be saved with any tasks.  First save a row, then add tasks to it by saving individual tasks with a reference to the rowId.  Thanks!");
		}
	}

	@Test
	void canSaveNewRowWithNullTaskList() throws Exception {
		Row newRow = new Row(projectPlanId, "new row title", null);
		newRow.setTaskList(null);
		this.rowService.saveNewRow(newRow);

		int rowCount = findCountOfRows();
		assertThat(rowCount).isEqualTo(1);

		boolean found = canFindRowWithTitle("new row title");
		assertThat(found).isTrue();
	}

	@Test
	void canGetAllRows() {
		createRowWithSQLOnly(projectPlanId, "ohai");
		createRowWithSQLOnly(projectPlanId, "there");

		List<Row> rows = rowService.getAllRows();

		assertThat(rows.size()).isEqualTo(2);

		assertThat(rows.get(0).getTitle()).isEqualTo("ohai");
		assertThat(rows.get(0).getId()).isNotNull();

		assertThat(rows.get(1).getTitle()).isEqualTo("there");
		assertThat(rows.get(1).getId()).isNotNull();
	}

	@Test
	void gettingRowsIncludesAllTasksAssociatedWithEach() {
		Long id1 = createRowWithSQLOnly(projectPlanId, "ohai");
		Long id2 = createRowWithSQLOnly(projectPlanId, "there");

		createTaskWithSQLOnly(id1, "first");
		createTaskWithSQLOnly(id2, "second1");
		createTaskWithSQLOnly(id2, "second2");

		List<Row> rows = rowService.getAllRows();

		assertThat(rows.size()).isEqualTo(2);

		assertThat(rows.get(0).getTaskList().size()).isEqualTo(1);
		assertThat(rows.get(0).getTaskList().get(0).getTitle()).isEqualTo("first");
		assertThat(rows.get(0).getTaskList().get(0).getRowId()).isEqualTo(rows.get(0).getId());

		assertThat(rows.get(1).getTaskList().size()).isEqualTo(2);
		assertThat(rows.get(1).getTaskList().get(0).getTitle()).isEqualTo("second1");
		assertThat(rows.get(1).getTaskList().get(0).getRowId()).isEqualTo(rows.get(1).getId());

		assertThat(rows.get(1).getTaskList().get(1).getTitle()).isEqualTo("second2");
		assertThat(rows.get(1).getTaskList().get(1).getRowId()).isEqualTo(rows.get(1).getId());
	}

	@Test
	void canUpdateARowsTitle() {
		Long id = createRowWithSQLOnly(projectPlanId, "ohai");
		RowPatchTemplate row = new RowPatchTemplate(id, "new title", null);

		rowService.patchRow(row);

		List<Row> allRows = rowService.getAllRows();
		assertThat(allRows.size()).isEqualTo(1);
		assertThat(allRows.get(0).getTitle()).isEqualTo("new title");
	}

	@Test
	void canUpdateARowsSingleTasksSizeAndPositionWithPatch() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "ohai");
		Long taskId = createTaskWithSQLOnly(rowId, "task 1", 1, 1);

		RowPatchTemplateTask taskTemplate = new RowPatchTemplateTask(taskId, 2, 5);
		RowPatchTemplate row = new RowPatchTemplate(rowId, "ohai", List.of(taskTemplate));

		rowService.patchRow(row);

		List<Row> allRows = rowService.getAllRows();

		assertThat(allRows.size()).isEqualTo(1);
		Row returnedRow = allRows.get(0);
		assertThat(returnedRow.getTitle()).isEqualTo("ohai");

		assertThat(returnedRow.getTaskList().size()).isEqualTo(1);
		Task returnedTask = returnedRow.getTaskList().get(0);
		assertThat(returnedTask.getId()).isEqualTo(taskId);
		assertThat(returnedTask.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask.getTitle()).isEqualTo("task 1");
		assertThat(returnedTask.getSize()).isEqualTo(2);
		assertThat(returnedTask.getPosition()).isEqualTo(5);
	}

	@Test
	void nullTitleInPatchDoNotChangeTheTitle() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "ohai");
		Long taskId = createTaskWithSQLOnly(rowId, "task 1", 1, 1);

		RowPatchTemplateTask taskTemplate = new RowPatchTemplateTask(taskId, 2, 5);
		RowPatchTemplate row = new RowPatchTemplate(rowId, null, List.of(taskTemplate));

		rowService.patchRow(row);

		List<Row> allRows = rowService.getAllRows();

		assertThat(allRows.size()).isEqualTo(1);
		Row returnedRow = allRows.get(0);
		assertThat(returnedRow.getTitle()).isEqualTo("ohai");
	}

	@Test
	void forRowPatchANullOrEmptyTaskListDoesNotChangeAnyTasks() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "ohai");
		Long taskId = createTaskWithSQLOnly(rowId, "task 1", 1, 2);

		RowPatchTemplate rowWithEmpty = new RowPatchTemplate(rowId, "new title", new ArrayList<>());
		rowService.patchRow(rowWithEmpty);

		RowPatchTemplate rowWithNull = new RowPatchTemplate(rowId, "new title", new ArrayList<>());
		rowService.patchRow(rowWithNull);


		List<Row> allRows = rowService.getAllRows();

		assertThat(allRows.size()).isEqualTo(1);
		Row returnedRow = allRows.get(0);
		List<Task> taskList = returnedRow.getTaskList();
		assertThat(taskList.size()).isEqualTo(1);

		Task taskThatShouldNotBeChanged = taskList.get(0);
		assertThat(taskThatShouldNotBeChanged.getId()).isEqualTo(taskId);
		assertThat(taskThatShouldNotBeChanged.getRowId()).isEqualTo(rowId);
		assertThat(taskThatShouldNotBeChanged.getTitle()).isEqualTo("task 1");
		assertThat(taskThatShouldNotBeChanged.getSize()).isEqualTo(1);
		assertThat(taskThatShouldNotBeChanged.getPosition()).isEqualTo(2);
	}

	@Test
	void taskBeingChangedInARowPatchDoesNotMakeAnyChangeForNullSize() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "ohai");
		Long taskId = createTaskWithSQLOnly(rowId, "task 1", 1, 2);

		RowPatchTemplateTask taskTemplate = new RowPatchTemplateTask(taskId, null, 3);
		RowPatchTemplate rowTemplate = new RowPatchTemplate(rowId, "new title", List.of(taskTemplate));
		rowService.patchRow(rowTemplate);


		List<Row> allRows = rowService.getAllRows();

		assertThat(allRows.size()).isEqualTo(1);
		Row returnedRow = allRows.get(0);
		List<Task> taskList = returnedRow.getTaskList();
		assertThat(taskList.size()).isEqualTo(1);

		Task changedTask = taskList.get(0);
		assertThat(changedTask.getId()).isEqualTo(taskId);
		assertThat(changedTask.getRowId()).isEqualTo(rowId);
		assertThat(changedTask.getTitle()).isEqualTo("task 1");
		assertThat(changedTask.getSize()).isEqualTo(1);
		assertThat(changedTask.getPosition()).isEqualTo(3);
	}

	@Test
	void taskBeingChangedInARowPatchDoesNotMakeAnyChangeForNullPosition() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "ohai");
		Long taskId = createTaskWithSQLOnly(rowId, "task 1", 1, 2);

		RowPatchTemplateTask taskTemplate = new RowPatchTemplateTask(taskId, 3, null);
		RowPatchTemplate rowTemplate = new RowPatchTemplate(rowId, "new title", List.of(taskTemplate));
		rowService.patchRow(rowTemplate);


		List<Row> allRows = rowService.getAllRows();

		assertThat(allRows.size()).isEqualTo(1);
		Row returnedRow = allRows.get(0);
		List<Task> taskList = returnedRow.getTaskList();
		assertThat(taskList.size()).isEqualTo(1);

		Task changedTask = taskList.get(0);
		assertThat(changedTask.getId()).isEqualTo(taskId);
		assertThat(changedTask.getRowId()).isEqualTo(rowId);
		assertThat(changedTask.getTitle()).isEqualTo("task 1");
		assertThat(changedTask.getSize()).isEqualTo(3);
		assertThat(changedTask.getPosition()).isEqualTo(2);
	}

	@Test
	void patchCanUpdateMultipleTasks() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "ohai");
		Long task1Id = createTaskWithSQLOnly(rowId, "task 1", 1, 2);
		Long task2Id = createTaskWithSQLOnly(rowId, "task 2", 3, 4);

		RowPatchTemplateTask task1Template = new RowPatchTemplateTask(task1Id, 2, 1);
		RowPatchTemplateTask task2Template = new RowPatchTemplateTask(task2Id, 4, 5);
		RowPatchTemplate row = new RowPatchTemplate(rowId, null, List.of(task1Template, task2Template));

		rowService.patchRow(row);

		List<Row> allRows = rowService.getAllRows();

		assertThat(allRows.size()).isEqualTo(1);
		Row returnedRow = allRows.get(0);
		assertThat(returnedRow.getTitle()).isEqualTo("ohai");

		assertThat(returnedRow.getTaskList().size()).isEqualTo(2);
		Task returnedTask1 = returnedRow.getTaskList().get(0);
		assertThat(returnedTask1.getId()).isEqualTo(task1Id);
		assertThat(returnedTask1.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask1.getTitle()).isEqualTo("task 1");
		assertThat(returnedTask1.getSize()).isEqualTo(2);
		assertThat(returnedTask1.getPosition()).isEqualTo(1);

		Task returnedTask2 = returnedRow.getTaskList().get(1);
		assertThat(returnedTask2.getId()).isEqualTo(task2Id);
		assertThat(returnedTask2.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask2.getTitle()).isEqualTo("task 2");
		assertThat(returnedTask2.getSize()).isEqualTo(4);
		assertThat(returnedTask2.getPosition()).isEqualTo(5);
	}

	@Test
	void ifARowHasSeveralTasksButOnlySomeAreMentionedForPatchThenTheOthersRemainOk() {

		Long rowId = createRowWithSQLOnly(projectPlanId, "ohai");
		Long task1Id = createTaskWithSQLOnly(rowId, "task 1", 1, 2);
		Long task2Id = createTaskWithSQLOnly(rowId, "task 2", 1, 4);
		Long task3Id = createTaskWithSQLOnly(rowId, "task 3", 1, 6);

		RowPatchTemplateTask task1Template = new RowPatchTemplateTask(task1Id, 2, 1);
		RowPatchTemplateTask task3Template = new RowPatchTemplateTask(task3Id, 4, 5);
		RowPatchTemplate row = new RowPatchTemplate(rowId, null, List.of(task1Template, task3Template));

		rowService.patchRow(row);

		List<Row> allRows = rowService.getAllRows();

		assertThat(allRows.size()).isEqualTo(1);
		Row returnedRow = allRows.get(0);
		assertThat(returnedRow.getTitle()).isEqualTo("ohai");

		assertThat(returnedRow.getTaskList().size()).isEqualTo(3);
		Task returnedTask1 = returnedRow.getTaskList().get(0);
		assertThat(returnedTask1.getId()).isEqualTo(task1Id);
		assertThat(returnedTask1.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask1.getTitle()).isEqualTo("task 1");
		assertThat(returnedTask1.getSize()).isEqualTo(2);
		assertThat(returnedTask1.getPosition()).isEqualTo(1);

		Task returnedTask2 = returnedRow.getTaskList().get(1);
		assertThat(returnedTask2.getId()).isEqualTo(task2Id);
		assertThat(returnedTask2.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask2.getTitle()).isEqualTo("task 2");
		assertThat(returnedTask2.getSize()).isEqualTo(1);
		assertThat(returnedTask2.getPosition()).isEqualTo(4);

		Task returnedTask3 = returnedRow.getTaskList().get(2);
		assertThat(returnedTask3.getId()).isEqualTo(task3Id);
		assertThat(returnedTask3.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask3.getTitle()).isEqualTo("task 3");
		assertThat(returnedTask3.getSize()).isEqualTo(4);
		assertThat(returnedTask3.getPosition()).isEqualTo(5);
	}

	@Test
	void ifAnErrorHappensEverythingIsRolledBack() {

		Long rowId = createRowWithSQLOnly(projectPlanId, "ohai");
		Long task1Id = createTaskWithSQLOnly(rowId, "task 1", 1, 2);
		Long task2Id = createTaskWithSQLOnly(rowId, "task 2", 1, 4);
		Long task3Id = createTaskWithSQLOnly(rowId, "task 3", 1, 6);

		RowPatchTemplateTask task1Template = new RowPatchTemplateTask(task1Id, 2, 1);
		RowPatchTemplateTask task2Template = new RowPatchTemplateTask(task1Id, 2, 3);
		RowPatchTemplateTask task3Template = new RowPatchTemplateTask(task3Id, 4, 5);
		RowPatchTemplate row = new RowPatchTemplate(rowId, "changed title", List.of(task1Template, task2Template, task3Template));

		// throw an exception on the 3rd task being updated -> no updates should happen, we want this to be an atomic operation (all or nothing!)
		doThrow(new RuntimeException("sometheing went wrong")).when(taskRepository).findById(task3Id);

		try {
			rowService.patchRow(row);
			fail("expecting exception");
		} catch (RuntimeException e) {
			// swallow the exception, we want to test the resulting DB state :)
		}

		List<Row> allRows = rowService.getAllRows();

		assertThat(allRows.size()).isEqualTo(1);
		Row returnedRow = allRows.get(0);
		assertThat(returnedRow.getTitle()).isEqualTo("ohai");

		assertThat(returnedRow.getTaskList().size()).isEqualTo(3);
		Task returnedTask1 = returnedRow.getTaskList().get(0);
		assertThat(returnedTask1.getId()).isEqualTo(task1Id);
		assertThat(returnedTask1.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask1.getTitle()).isEqualTo("task 1");
		assertThat(returnedTask1.getSize()).isEqualTo(1);
		assertThat(returnedTask1.getPosition()).isEqualTo(2);

		Task returnedTask2 = returnedRow.getTaskList().get(1);
		assertThat(returnedTask2.getId()).isEqualTo(task2Id);
		assertThat(returnedTask2.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask2.getTitle()).isEqualTo("task 2");
		assertThat(returnedTask2.getSize()).isEqualTo(1);
		assertThat(returnedTask2.getPosition()).isEqualTo(4);

		Task returnedTask3 = returnedRow.getTaskList().get(2);
		assertThat(returnedTask3.getId()).isEqualTo(task3Id);
		assertThat(returnedTask3.getRowId()).isEqualTo(rowId);
		assertThat(returnedTask3.getTitle()).isEqualTo("task 3");
		assertThat(returnedTask3.getSize()).isEqualTo(1);
		assertThat(returnedTask3.getPosition()).isEqualTo(6);
	}

	@Test
	void throwsAnErrorIfProjectConsistencyValidatorDoes() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "sup");
		Long task1Id = createTaskWithSQLOnly(rowId, "task 1", 1, 2);

		RowPatchTemplateTask task1Template = new RowPatchTemplateTask(task1Id, 2, 1);
		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(rowId, null, List.of(task1Template));

		assertThat(rowService.getAllRows().size()).isEqualTo(1);
		Row row = rowService.getAllRows().get(0);

		doThrow(new RuntimeException("some message")).when(projectConsistencyValidator).validateRowChangeSetThrowingExceptions(any(), eq(rowPatchTemplate));

		try {
			rowService.patchRow(rowPatchTemplate);
			fail("expecting exception");
		} catch(RuntimeException e) {
			assertThat(e.getClass()).isEqualTo(RuntimeException.class);
			assertThat(e.getMessage()).isEqualTo("some message");
		}
	}

	@Test
	void updateDoesNotWorkOnARowWithANonexistentId() {
		try {
			Long id = createRowWithSQLOnly(projectPlanId, "ohai");
			RowPatchTemplate row = new RowPatchTemplate(id + 1, "new title", null);

			rowService.patchRow(row);
			fail("Expecting exception");

		} catch (RowServiceValidator.RowNotFoundException e) {
			assertThat(e.getMessage()).contains("The rowId passed does not exist!  It is impossible to perform an update on a row that does not exist.");

		} catch (RuntimeException e) {
			fail("expecting a RowNotFoundException - instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void updateDoesNotWorkWithNullId() {
		try {
			RowPatchTemplate row = new RowPatchTemplate(null, "new title", null);

			rowService.patchRow(row);
			fail("Expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).contains("Cannot update a row that does not have an id specified.  Maybe you meant to save a new row, instead of an update?");
		}
	}

	@Test
	void canDeleteAnEmptyRowById() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "new row");
		int rowCount = findCountOfRows();
		assertThat(rowCount).isEqualTo(1);

		rowService.deleteEmptyRowById(rowId);

		int rowCountAfterDelete = findCountOfRows();
		assertThat(rowCountAfterDelete).isEqualTo(0);
	}

	@Test
	void cannotDeleteARowThatHasTasksAssociatedWithIt() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "new row");
		createTaskWithSQLOnly(rowId, "some task");
		assertThat(findCountOfRows()).isEqualTo(1);
		assertThat(findTotalNumberOfTasks()).isEqualTo(1);

		try {
			rowService.deleteEmptyRowById(rowId);
			fail("expecting exception");

		} catch (RowServiceValidator.CannotDeleteNonEmptyRowException e) {

			assertThat(findCountOfRows()).isEqualTo(1);
			assertThat(findTotalNumberOfTasks()).isEqualTo(1);
			assertThat(e.getMessage()).isEqualTo("Cannot delete a row that has tasks that belong to it.  Please delete the tasks or move them to another row before deleting this row.");

		} catch (Exception e) {
			fail("expecting CannotDeleteNonEmptyRowException, instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void deleteReturnsTheObjectThatWasDeleted() {
		Long rowId = createRowWithSQLOnly(projectPlanId, "new row");
		int rowCount = findCountOfRows();
		assertThat(rowCount).isEqualTo(1);

		Row deletedRow = rowService.deleteEmptyRowById(rowId);

		assertThat(deletedRow.getId()).isEqualTo(rowId);
		assertThat(deletedRow.getTitle()).isEqualTo("new row");
		assertThat(deletedRow.getTaskList().size()).isEqualTo(0);
	}

	@Test
	void deleteErrorsIfTheRowIDIsNotValid() {
		try {
			rowService.deleteEmptyRowById(67L);
			fail("expecting exception");

		} catch (RowServiceValidator.RowNotFoundException e) {
			assertThat(e.getMessage()).isEqualTo("The rowId passed does not point to a valid row.  No changes were made.");

		} catch (Exception e) {
			fail("expecting a RowNotFoundException - instead got " + e.getClass().getCanonicalName());
		}
	}

	private int findCountOfRows() {
		try {
			Connection connection = this.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT * FROM P1_ROW");

			int count = 0;
			while (resultSet.next()) {
				count++;
			}
			resultSet.close();
			statement.close();
			connection.close();
			return count;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean canFindRowWithTitle(String titleToSearchFor) throws Exception {
		Connection connection = this.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM P1_ROW");

		boolean found = false;
		while (resultSet.next()) {
			if (titleToSearchFor.equals(resultSet.getString("TITLE"))) {
				found = true;
			}
		}
		resultSet.close();
		statement.close();
		connection.close();
		return found;
	}
}