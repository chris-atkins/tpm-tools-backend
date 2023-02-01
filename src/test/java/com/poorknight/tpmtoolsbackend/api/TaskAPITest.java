package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.response.APITask;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class TaskAPITest {

	@InjectMocks
	TaskAPI api;

	@Mock
	TaskService taskService;

	@Test
	void postTaskReturnsResponseFromTheServiceThatIncludesAnId() {
		String taskTitle = "new task 1";
		long idFromService = 5L;

		Task inputTask = new Task(taskTitle);
		Task savedTask = new Task(idFromService, taskTitle);
		Mockito.when(taskService.saveNewTask(inputTask)).thenReturn(savedTask);

		APITask response = api.postTask(new APITask(null, taskTitle));

		assertThat(response.getTitle()).isEqualTo(taskTitle);
		assertThat(response.getId()).isEqualTo(idFromService);
	}

	@Test
	void postTaskDoesNotAcceptAnId() {
		APITask task = new APITask(3L, "something");

		try {
			api.postTask(task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, do not provide an id.  Maybe you intended to use a PUT.\"");
		}
	}

	@Test
	void putTaskReturnsResponseFromServiceAfterCallingUpdate() {
		String taskTitle = "changed";
		long existingTaskId = 55L;

		Task inputTask = new Task(existingTaskId, taskTitle);
		Task savedTask = new Task(existingTaskId, taskTitle);
		Mockito.when(taskService.updateTask(inputTask)).thenReturn(savedTask);

		APITask response = api.putTask(55L, new APITask(existingTaskId, taskTitle));

		assertThat(response.getTitle()).isEqualTo(taskTitle);
		assertThat(response.getId()).isEqualTo(existingTaskId);
	}

	@Test
	void putTaskDoesNotAcceptATaskWithoutId() {
		APITask task = new APITask(null,"something");

		try {
			api.putTask(1L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide an id in the request body.\"");
		}
	}

	@Test
	void putTaskThrows404StyleExceptionIfDoesNotExist() {
		APITask task = new APITask(1L,"something");
		Mockito.when(taskService.updateTask(task.toDomainObject())).thenThrow(new TaskService.TaskNotFoundException("st"));

		try {
			api.putTask(1L, task);
			fail("expecting exception");
		} catch (ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(404);
			assertThat(e.getMessage()).contains("Cannot PUT the task.  No task exists with the passed id: 1");
		}
	}

	@Test
	void putTaskThrows500StyleExceptionIfServiceThrowsUnexpectedException() {
		APITask task = new APITask(1L,"something");
		Mockito.when(taskService.updateTask(task.toDomainObject())).thenThrow(new RuntimeException("st"));

		try {
			api.putTask(1L, task);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(500);
			assertThat(e.getMessage()).contains("Unexpected error encountered while attempting to update task with id 1.  Please try again, until a 200 message is returned.");
		}
	}

	@Test
	void putTaskDoesNotAcceptMismatchedIdsInUrlAndBody() {
		APITask task = new APITask(5L,"something");

		try {
			api.putTask(7L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, the url id and request body id must match.\"");
		}
	}

	@Test
	void getTasksRetrievesAllTasksFromServiceAndIncludesIds() {
		Task task1 = new Task(5L, "st");
		Task task2 = new Task(6L, "st else");
		List<Task> taskList = Arrays.asList(task1, task2);
		Mockito.when(taskService.getAllTasks()).thenReturn(taskList);

		List<APITask> apiTasks = api.getTasks();

		assertThat(apiTasks.size()).isEqualTo(2);
		assertThat(apiTasks.get(0)).isEqualTo(new APITask(5L, "st"));
		assertThat(apiTasks.get(1)).isEqualTo(new APITask(6L, "st else"));
	}

	@Test
	void getTasksReturnsAnEmptyListIfNoTasksExist() {
		List<Task> taskList = new LinkedList<>();
		Mockito.when(taskService.getAllTasks()).thenReturn(taskList);

		List<APITask> apiTasks = api.getTasks();

		assertThat(apiTasks).isNotNull();
		assertThat(apiTasks.size()).isEqualTo(0);
	}

	@Test
	void deleteTaskCallsServiceDeleteAndReturnsDeletedTask() {
		Mockito.when(taskService.deleteTask(55L)).thenReturn(new Task(55L, "st"));

		APITask deletedTask = api.deleteTask(55L);

		assertThat(deletedTask.getId()).isEqualTo(55L);
		assertThat(deletedTask.getTitle()).isEqualTo("st");
	}

	@Test
	void deleteTaskThrows404StyleExceptionIfServiceThrowsTaskNotFoundException() {
		Mockito.when(taskService.deleteTask(55L)).thenThrow(new TaskService.TaskNotFoundException("message should be independent"));

		try {
			APITask deletedTask = api.deleteTask(55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(404);
			assertThat(e.getMessage()).contains("Task with id 55 does not exist.");
		}
	}

	@Test
	void deleteTaskThrows500StyleExceptionIfServiceThrowsUnexpectedException() {
		Mockito.when(taskService.deleteTask(55L)).thenThrow(new RuntimeException("st"));

		try {
			APITask deletedTask = api.deleteTask(55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(500);
			assertThat(e.getMessage()).contains("Unexpected error encountered while attempting to delete task with id 55.  Please try again, until a 404 message is returned.");
		}
	}
}
