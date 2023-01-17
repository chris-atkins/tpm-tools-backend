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
	void canSaveANewTaskWithATitle() throws Exception {
		int originalCount = findTotalNumberOfTasks();

		taskService.saveNewTask(new Task("Work to do!"));

		int newCount = findTotalNumberOfTasks();
		boolean found = canFindTaskWithTitle("Work to do!");
		assertThat(newCount).isEqualTo(originalCount + 1);
		assertTrue(found);
	}

	@Test
	void saveTaskReturnsTheSavedTaskWithAnId() throws Exception {
		Task savedTask = taskService.saveNewTask(new Task("Work to do!"));
		assertThat(savedTask.getId()).isNotNull();
	}

	@Test
	void saveNewTaskDoesNotAllowIdToBeSet() throws Exception {
		assertThat(findTotalNumberOfTasks()).isEqualTo(0);

		Task task = new Task(2L, "Work to do!");

		try {
			taskService.saveNewTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Cannot specify an ID on a new Task!  Try the updateTask method instead :)");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}

	@Test
	void canGetAllTasks() throws Exception {
		int originalCount = findTotalNumberOfTasks();
		assertThat(originalCount).isEqualTo(0);

		taskService.saveNewTask(new Task("Another task"));
		taskService.saveNewTask(new Task("Even more work to do!"));

		List<Task> allTasks = taskService.getAllTasks();
		assertThat(allTasks.size()).isEqualTo(2);
		assertThat(allTasks.get(0).getTitle()).isEqualTo("Another task");
		assertThat(allTasks.get(1).getTitle()).isEqualTo("Even more work to do!");
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
		taskService.saveNewTask(new Task("One more thing"));

		List<Task> allTasks = taskService.getAllTasks();
		assertThat(allTasks.size()).isEqualTo(1);
		assertThat(allTasks.get(0).getTitle()).isEqualTo("One more thing");

		Task taskToUpdate = allTasks.get(0);
		taskToUpdate.setTitle("One more thing! :)");
		taskService.updateTask(taskToUpdate);

		boolean updatedTitleFound = canFindTaskWithTitle("One more thing! :)");
		assertThat(updatedTitleFound).isTrue();
		assertThat(findTotalNumberOfTasks()).isEqualTo(1);
	}

	@Test
	void updateTaskMustHaveAnIdPopulated() throws Exception {
		Task task = new Task("Work to do!");

		try {
			taskService.updateTask(task);
			Assertions.fail("Expecting an exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("Must specify an ID to update a Task - that is how we know what Task to update! Try the saveNewTask method instead :)");
			assertThat(findTotalNumberOfTasks()).isEqualTo(0);
		}
	}


	@Test
	void updateTaskReturnsTheSavedTaskWithSameId() throws Exception {
		Task savedTask = taskService.saveNewTask(new Task("Work to do!"));

		savedTask.setTitle("new title");
		Task updatedTask = taskService.updateTask(savedTask);

		assertThat(updatedTask.getId()).isEqualTo(savedTask.getId());
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

}