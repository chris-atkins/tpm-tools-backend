package com.poorknight.tpmtoolsbackend.domain.tasks;

import com.poorknight.tpmtoolsbackend.domain.BaseUnitTestWithDatabase;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.RowService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest extends BaseUnitTestWithDatabase {

	@Autowired
	private TaskService taskService;

	@Autowired
	private RowService rowService;

	private Long projectPlanId;

	@BeforeEach
	public void setUp() {
		this.deleteAllTasksAndRowsAndProjectPlans();
		projectPlanId = this.createProjectPlanWithSQLOnly("plan");
	}


	@Test
	void canSaveANewTaskWithARowIdAndTitleAndSizeAndPosition() throws Exception {
		int originalCount = findTotalNumberOfTasks();

		Long rowId = createRowWithSQLOnly(projectPlanId, "test row");
		taskService.saveNewTask(new Task(rowId, "Work to do!", 33, 2));

		int newCount = findTotalNumberOfTasks();
		boolean found = canFindTaskWithTitle("Work to do!");
		assertThat(newCount).isEqualTo(originalCount + 1);
		assertTrue(found);
	}

	@Test
	void saveTaskReturnsTheSavedTaskWithAnIdAndSameTitleAndSizeAndPosition() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task savedTask = taskService.saveNewTask(new Task(row.getId(), "Work to do!", 27, 2));

		assertThat(savedTask.getId()).isNotNull();
		assertThat(savedTask.getTitle()).isEqualTo("Work to do!");
		assertThat(savedTask.getSize()).isEqualTo(27);
		assertThat(savedTask.getPosition()).isEqualTo(2);
		assertThat(savedTask.getRowId()).isEqualTo(row.getId());
	}

	@Test
	void saveNewTaskDoesNotAllowIdToBeSet() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Task task = new Task(3L,2L, "Work to do!", 1, 2);

		try {
			taskService.saveNewTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Cannot specify an ID on a new Task!  Try the updateTask method instead :)");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void saveNewTaskRequiresASizeToBeSet() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task task = new Task(row.getId(), "Work to do!", null, 1);

		try {
			taskService.saveNewTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a size for a new Task.  No action taken.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void saveNewTaskRequiresAPositionToBeSet() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task task = new Task(row.getId(), "Work to do!", 1, null);

		try {
			taskService.saveNewTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a position for a new Task.  No action taken.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void saveNewTaskRequiresARowIdToBeSet() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Task task = new Task(null, "Work to do!", 33, 2);

		try {
			taskService.saveNewTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a rowId for a new Task.  No action taken.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void saveNewTaskRequiresATitleToBeSet() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task task = new Task(row.getId(), null, 55, 5);

		try {
			taskService.saveNewTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a title for a new Task. An empty string is ok, null is not.  No action taken.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void saveNewTaskWithEmptyTitleIsOk() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task task = new Task(row.getId(), "", 55, 1);

		Task savedTask = taskService.saveNewTask(task);

		assertThat(savedTask.getTitle()).isEmpty();
	}

	@Test
	void canGetAllTasksForARow() throws Exception {
		int originalCount = findTotalNumberOfTasks();
		assertThat(originalCount).isEqualTo(0);

		Row row1 = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		taskService.saveNewTask(new Task(row1.getId(), "Another task", 1, 1));
		taskService.saveNewTask(new Task(row1.getId(), "Even more work to do!", 2, 2));

		Row row2 = rowService.saveNewRow(new Row(projectPlanId, "test row 2"));
		taskService.saveNewTask(new Task(row2.getId(), "Another task again", 1, 1));

		List<Task> allTasks = taskService.getAllTasksForRow(row1.getId());

		assertThat(allTasks.size()).isEqualTo(2);
		assertThat(allTasks.get(0).getRowId()).isEqualTo(row1.getId());
		assertThat(allTasks.get(0).getTitle()).isEqualTo("Another task");
		assertThat(allTasks.get(0).getSize()).isEqualTo(1);

		assertThat(allTasks.get(1).getRowId()).isEqualTo(row1.getId());
		assertThat(allTasks.get(1).getTitle()).isEqualTo("Even more work to do!");
		assertThat(allTasks.get(1).getSize()).isEqualTo(2);
	}

	@Test
	void getAllTasksReturnsEmptyListIfNoTasksExist() throws Exception {
		int originalCount = findTotalNumberOfTasks();
		assertThat(originalCount).isEqualTo(0);

		List<Task> allTasks = taskService.getAllTasksForRow(-1L);

		assertThat(allTasks).isNotNull();
		assertThat(allTasks.size()).isEqualTo(0);
	}

	@Test
	void canUpdateAnExistingTask() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		taskService.saveNewTask(new Task(row.getId(), "One more thing", 1, 1));

		List<Task> allTasks = taskService.getAllTasksForRow(row.getId());
		assertThat(allTasks.size()).isEqualTo(1);
		assertThat(allTasks.get(0).getTitle()).isEqualTo("One more thing");
		assertThat(allTasks.get(0).getSize()).isEqualTo(1);

		Task taskToUpdate = allTasks.get(0);
		taskToUpdate.setTitle("One more thing! :)");
		taskToUpdate.setSize(5);
		taskService.updateTask(taskToUpdate);

		Task foundTask = findTaskWithTitle("One more thing! :)");
		assertThat(foundTask).isNotNull();
		assertThat(foundTask.getSize()).isEqualTo(5);
		assertThat(findTotalNumberOfTasks()).isEqualTo(1);
	}

	@Test
	void updateTaskMustHaveAnIdPopulated() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task task = new Task(row.getId(), "Work to do!", 1, 2);

		try {
			taskService.updateTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify an ID to update a Task - that is how we know what Task to update! Try the saveNewTask method instead :)");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void updateTaskMustHaveARowIdPopulated() throws Exception {
		Task task = new Task(1L, null, "Work to do!", 1, 4);

		try {
			taskService.updateTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a rowId when updating a Task.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void updateTaskMustHaveASizePopulated() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task task = new Task(1L, row.getId(), "Work to do!", null, 3);

		try {
			taskService.updateTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a size while updating a Task. A full task must be given, including fields that are not changing.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void updateTaskMustHaveAPositionPopulated() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task task = new Task(1L, row.getId(), "Work to do!", 3, null);

		try {
			taskService.updateTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a position while updating a Task. A full task must be given, including fields that are not changing.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void updateTaskDoesNotAllowNullTitle() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task task = new Task(1L, row.getId(), null, 4, 34);

		try {
			taskService.updateTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a title while updating a Task. A full task must be given, including fields that are not changing.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void updatingATaskToEmptyStringIsOk() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task savedTask = taskService.saveNewTask(new Task(row.getId(), "One more thing", 1, 8));
		Task task = new Task(savedTask.getId(), row.getId(), "", 1, 8);

		Task updatedTask = taskService.updateTask(task);
		assertThat(updatedTask.getTitle()).isEmpty();
	}

	@Test
	void updateTaskReturnsTheSavedTaskWithSameId() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task savedTask = taskService.saveNewTask(new Task(row.getId(), "Work to do!", 1, 1));

		savedTask.setTitle("new title");
		Task updatedTask = taskService.updateTask(savedTask);

		assertThat(updatedTask.getId()).isEqualTo(savedTask.getId());
	}

	@Test
	void updateTaskThrowsExceptionIfIdDoesNotExist() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task savedTask = new Task(88L, row.getId(), "Work to do!", 1, 5);

		try {
			Task updatedTask = taskService.updateTask(savedTask);
			fail("expecting exception");
		} catch (TaskService.TaskNotFoundException e) {
			assertThat(e.getMessage()).contains("Cannot update task with id 88. It does not exist.");
		} catch (Exception e) {
			fail("got the wrong type of exception");
		}
	}

	@Test
	void canDeleteATask() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task savedTask = taskService.saveNewTask(new Task(row.getId(), "Work to do!", 1, 1));
		assertThat(findTotalNumberOfTasks()).isEqualTo(1);

		taskService.deleteTask(savedTask.getId());

		assertThat(findTotalNumberOfTasks()).isEqualTo(0);
	}

	@Test
	void deletingATaskReturnsTheDeletedTask() throws Exception {
		Row row = rowService.saveNewRow(new Row(projectPlanId, "test row"));
		Task savedTask = taskService.saveNewTask(new Task(row.getId(), "Work to do!", 55, 2));
		assertThat(findTotalNumberOfTasks()).isEqualTo(1);

		Task deletedTask = taskService.deleteTask(savedTask.getId());

		assertThat(deletedTask.getId()).isEqualTo(savedTask.getId());
		assertThat(deletedTask.getRowId()).isEqualTo(row.getId());
		assertThat(deletedTask.getTitle()).isEqualTo("Work to do!");
		assertThat(deletedTask.getSize()).isEqualTo(55);
		assertThat(deletedTask.getPosition()).isEqualTo(2);
	}

	@Test
	void deletingANonExistentTaskThrowsError() throws Exception {

		try {
			taskService.deleteTask(55L);
			fail("Expecting exception");
		} catch (TaskService.TaskNotFoundException e) {
			assertThat(e.getMessage()).contains("Cannot delete task with id 55. It does not exist.");
		} catch (Exception e) {
			fail("wrong type of exception thrown");
		}
	}

	@Test
	void canFindASingleTaskById() {
		Long rowId = this.createRowWithSQLOnly(projectPlanId, "row");
		Long taskId = this.createTaskWithSQLOnly(rowId, "tittle");

		Task foundTask = taskService.findTaskWithId(taskId);

		assertThat(foundTask.getRowId()).isEqualTo(rowId);
		assertThat(foundTask.getTitle()).isEqualTo("tittle");
	}

	@Test
	void ifAttemptingToFindATaskByIdAnExceptionIsThrownIfItDoesntExist() {
		try {
			taskService.findTaskWithId(-1L);
			fail("expecting exception");

		} catch (TaskService.TaskNotFoundException e) {
			assertThat(e.getMessage()).contains("Cannot find task with id -1.");
		}

	}

	private boolean canFindTaskWithTitle(String titleToSearchFor) throws Exception {
		Connection connection = this.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM TASK");

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


	private Task findTaskWithTitle(String titleToSearchFor) throws Exception {
		Connection connection = this.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM TASK");

		Task task = null;
		while (resultSet.next()) {
			if (titleToSearchFor.equals(resultSet.getString("TITLE"))) {
				task = new Task(
						resultSet.getLong("ID"),
						resultSet.getString("TITLE"),
						resultSet.getInt("SIZE"),
						resultSet.getInt("POSITION"));
			}
		}
		resultSet.close();
		statement.close();
		connection.close();
		return task;
	}
}