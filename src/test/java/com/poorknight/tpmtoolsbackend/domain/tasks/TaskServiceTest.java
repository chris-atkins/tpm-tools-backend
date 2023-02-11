package com.poorknight.tpmtoolsbackend.domain.tasks;

import com.poorknight.tpmtoolsbackend.domain.BaseUnitTestWithDatabase;
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

	@BeforeEach
	public void setUp() {
		this.deleteAllTasks();
	}


	@Test
	void canSaveANewTaskWithATitleAndSize() throws Exception {
		int originalCount = findTotalNumberOfTasks();

		taskService.saveNewTask(new Task("Work to do!", 1));

		int newCount = findTotalNumberOfTasks();
		boolean found = canFindTaskWithTitle("Work to do!");
		assertThat(newCount).isEqualTo(originalCount + 1);
		assertTrue(found);
	}


	@Test
	void saveTaskReturnsTheSavedTaskWithAnIdAndSameTitleAndSize() throws Exception {
		Task savedTask = taskService.saveNewTask(new Task("Work to do!", 1));
		assertThat(savedTask.getId()).isNotNull();
		assertThat(savedTask.getTitle()).isEqualTo("Work to do!");
		assertThat(savedTask.getId()).isEqualTo(1);
	}


	@Test
	void saveNewTaskDoesNotAllowIdToBeSet() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Task task = new Task(2L, "Work to do!", 1);

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

		Task task = new Task("Work to do!", null);

		try {
			taskService.saveNewTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a size for a new Task.  No action taken.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}


	@Test
	void saveNewTaskRequiresATitleToBeSet() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Task task = new Task(null, 55);

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
		Task task = new Task("", 55);
		Task savedTask = taskService.saveNewTask(task);
		assertThat(savedTask.getTitle()).isEmpty();
	}


	@Test
	void canGetAllTasks() throws Exception {
		int originalCount = findTotalNumberOfTasks();
		assertThat(originalCount).isEqualTo(0);

		taskService.saveNewTask(new Task("Another task", 1));
		taskService.saveNewTask(new Task("Even more work to do!", 2));

		List<Task> allTasks = taskService.getAllTasks();
		assertThat(allTasks.size()).isEqualTo(2);
		assertThat(allTasks.get(0).getTitle()).isEqualTo("Another task");
		assertThat(allTasks.get(0).getSize()).isEqualTo(1);
		assertThat(allTasks.get(1).getTitle()).isEqualTo("Even more work to do!");
		assertThat(allTasks.get(1).getSize()).isEqualTo(2);
	}


	@Test
	void getAllTasksReturnsEmptyListIfNoTasksExist() throws Exception {
		int originalCount = findTotalNumberOfTasks();
		assertThat(originalCount).isEqualTo(0);

		List<Task> allTasks = taskService.getAllTasks();

		assertThat(allTasks).isNotNull();
		assertThat(allTasks.size()).isEqualTo(0);
	}


	@Test
	void canUpdateAnExistingTask() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		taskService.saveNewTask(new Task("One more thing", 1));

		List<Task> allTasks = taskService.getAllTasks();
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
		Task task = new Task("Work to do!", 1);

		try {
			taskService.updateTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify an ID to update a Task - that is how we know what Task to update! Try the saveNewTask method instead :)");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}


	@Test
	void updateTaskMustHaveASizePopulated() throws Exception {
		Task task = new Task(1L,"Work to do!", null);

		try {
			taskService.updateTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify a size while updating a Task. A full task must be given, including fields that are not changing.");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}


	@Test
	void updateTaskDoesNotAllowNullTitle() throws Exception {
		Task task = new Task(1L,null, 4);

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
		Task savedTask = taskService.saveNewTask(new Task("One more thing", 1));
		Task task = new Task(savedTask.getId(),"", 1);

		Task updatedTask = taskService.updateTask(task);
		assertThat(updatedTask.getTitle()).isEmpty();
	}


	@Test
	void updateTaskReturnsTheSavedTaskWithSameId() throws Exception {
		Task savedTask = taskService.saveNewTask(new Task("Work to do!", 1));

		savedTask.setTitle("new title");
		Task updatedTask = taskService.updateTask(savedTask);

		assertThat(updatedTask.getId()).isEqualTo(savedTask.getId());
	}


	@Test
	void updateTaskThrowsExceptionIfIdDoesNotExist() throws Exception {
		Task savedTask = new Task(12L, "Work to do!", 1);

		try {
			Task updatedTask = taskService.updateTask(savedTask);
			fail("expecting exception");
		} catch (TaskService.TaskNotFoundException e) {
			assertThat(e.getMessage()).contains("Cannot update task with id 12. It does not exist.");
		} catch (Exception e) {
			fail("got the wrong type of excpetion");
		}
	}


	@Test
	void canDeleteATask() throws Exception {
		Task savedTask = taskService.saveNewTask(new Task("Work to do!", 1));
		assertThat(findTotalNumberOfTasks()).isEqualTo(1);

		taskService.deleteTask(savedTask.getId());

		assertThat(findTotalNumberOfTasks()).isEqualTo(0);
	}


	@Test
	void deletingATaskReturnsTheDeletedTask() throws Exception {
		Task savedTask = taskService.saveNewTask(new Task("Work to do!", 55));
		assertThat(findTotalNumberOfTasks()).isEqualTo(1);

		Task deletedTask = taskService.deleteTask(savedTask.getId());

		assertThat(deletedTask.getId()).isEqualTo(savedTask.getId());
		assertThat(deletedTask.getTitle()).isEqualTo("Work to do!");
		assertThat(deletedTask.getSize()).isEqualTo(55);
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


	private int findTotalNumberOfTasks() throws Exception {
		Connection connection = this.getConnection();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM TASK");

		int count = 0;
		while (resultSet.next()) {
			count++;
		}
		resultSet.close();
		statement.close();
		connection.close();
		return count;
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
						resultSet.getInt("SIZE"));
			}
		}
		resultSet.close();
		statement.close();
		connection.close();
		return task;
	}

}