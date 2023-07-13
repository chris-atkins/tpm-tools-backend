package com.poorknight.tpmtoolsbackend.domain.row;

import com.poorknight.tpmtoolsbackend.domain.BaseUnitTestWithDatabase;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class RowServiceTest extends BaseUnitTestWithDatabase {

	@Autowired
	private RowService rowService;

	private Long projectPlanId;

	@BeforeEach
	public void setUp() {
		this.deleteAllTasksAndRowsAndProjectPlans();
		projectPlanId = createProjectPlanWithSQLOnly("new plan");
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
		RowPatch row = new RowPatch(id, "new title");

		rowService.updateRow(row);

		List<Row> allRows = rowService.getAllRows();
		assertThat(allRows.get(0).getTitle()).isEqualTo("new title");
	}


	@Test
	void updateDoesNotWorkOnARowWithANonexistentId() {
		try {
			Long id = createRowWithSQLOnly(projectPlanId, "ohai");
			RowPatch row = new RowPatch(id + 1, "new title");

			rowService.updateRow(row);
			fail("Expecting exception");

		} catch (RowService.RowNotFoundException e) {
			assertThat(e.getMessage()).contains("The rowId passed does not exist!  It is impossible to perform an update on a row that does not exist.");

		} catch (RuntimeException e) {
			fail("expecting a RowNotFoundException - instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void updateDoesNotWorkWithNullId() {
		try {
			RowPatch row = new RowPatch(null, "new title");

			rowService.updateRow(row);
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

		} catch (RowService.CannotDeleteNonEmptyRowException e) {

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

		} catch (RowService.RowNotFoundException e) {
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