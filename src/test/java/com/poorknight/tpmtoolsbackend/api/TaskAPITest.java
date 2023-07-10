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
import org.springframework.http.HttpStatus;
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
		Integer taskSize = 4;
		long idFromService = 5L;
		Long rowId = 55L;
		Integer taskPosition = 3;

		Task inputTask = new Task(rowId, taskTitle, taskSize, taskPosition);
		Task savedTask = new Task(idFromService, rowId, taskTitle, taskSize, taskPosition);
		Mockito.when(taskService.saveNewTask(inputTask)).thenReturn(savedTask);

		APITask response = api.postTask(rowId, new APITask(null, rowId, taskTitle, taskSize, taskPosition));

		assertThat(response.getId()).isEqualTo(idFromService);
		assertThat(response.getRowId()).isEqualTo(rowId);
		assertThat(response.getTitle()).isEqualTo(taskTitle);
		assertThat(response.getSize()).isEqualTo(taskSize);
	}

	@Test
	void postTaskDoesNotAcceptAnId() {
		APITask task = new APITask(3L, 55L, "something", 4, 3);

		try {
			api.postTask(55L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, do not provide an id.  Maybe you intended to use a PUT.\"");
		}
	}

	@Test
	void postTaskRequiresRowId() {
		APITask task = new APITask(null, null, "hi", 4, 3);

		try {
			api.postTask(55L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a rowId is mandatory.\"");
		}
	}

	@Test
	void postTaskRequiresTitle() {
		APITask task = new APITask(null, 55L, null, 4, 3);

		try {
			api.postTask(55L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a title is mandatory.  An empty string is valid.\"");
		}
	}

	@Test
	void postTaskRequiresSize() {
		APITask task = new APITask(null, 55L, "something", null, 3);

		try {
			api.postTask(55L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a positive integer for size is mandatory.\"");
		}
	}

	@Test
	void postTaskRequiresPosition() {
		APITask task = new APITask(null, 55L, "something", 5, null);

		try {
			api.postTask(55L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a positive integer for position is mandatory.\"");
		}
	}

	@Test
	void postTaskRequiresRowPathToBeTheSameAsBodyRowId() {
		APITask task = new APITask(null, 55L, "something", 6, 5);

		try {
			api.postTask(1234L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, the rowId in the body must match the one in the url.\"");
		}
	}

	@Test
	void postTaskRequiresAPositiveSize() {
		try {
			api.postTask(55L, new APITask(null, 55L, "something", 0, 5));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a positive integer for size is mandatory.\"");
		}

		try {
			api.postTask(55L, new APITask(null, 55L, "something", -1, 5));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a positive integer for size is mandatory.\"");
		}
	}


	@Test
	void postTaskRequiresAPositivePosition() {
		try {
			api.postTask(55L, new APITask(null, 55L, "something", 5, 0));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a positive integer for position is mandatory.\"");
		}

		try {
			api.postTask(55L, new APITask(null, 55L, "something", 5, -1));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a positive integer for position is mandatory.\"");
		}
	}

	@Test
	void putTaskReturnsResponseFromServiceAfterCallingUpdate() {
		Mockito.when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 27L, "st", 1, 3));

		Task inputTask = new Task(55L, 27L, "changed", 4, 3);
		Task savedTask = new Task(55L, 27L, "changed", 4, 3);
		Mockito.when(taskService.updateTask(inputTask)).thenReturn(savedTask);

		APITask response = api.putTask(27L, 55L, new APITask(55L, 27L, "changed", 4, 3));

		assertThat(response.getTitle()).isEqualTo("changed");
		assertThat(response.getSize()).isEqualTo((Integer) 4);
		assertThat(response.getId()).isEqualTo((Long) 55L);
	}

	@Test
	void putTaskDoesNotAcceptATaskWithoutId() {
		APITask task = new APITask(null,4L, "something", 4, 3);

		try {
			api.putTask(4L, 1L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide an id in the request body.\"");
		}
	}

	@Test
	void putTaskDoesNotAcceptATaskWithoutRowId() {
		APITask task = new APITask(1L,null, "title", 4, 3);

		try {
			api.putTask(4L, 1L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide a rowId.\"");
		}
	}

	@Test
	void putTaskAllowsMovingTasksBetweenRowsByPassingAMismatchingRowId() {
		Mockito.when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 27L, "st", 4, 3));

		Task inputTask = new Task(55L, 8624L, "changed", 4, 3);
		Task savedTask = new Task(55L, 8624L, "changed", 4, 3);
		Mockito.when(taskService.updateTask(inputTask)).thenReturn(savedTask);

		APITask response = api.putTask(27L, 55L, new APITask(55L, 8624L, "changed", 4, 3));

		assertThat(response.getTitle()).isEqualTo("changed");
		assertThat(response.getSize()).isEqualTo((Integer) 4);
		assertThat(response.getId()).isEqualTo((Long) 55L);
	}

	@Test
	void putTaskOriginalRowIdMustMatchUrlEvenIfChangingTheRow() {
		Mockito.when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 1L, "st", 1, 2));

		try {
			api.putTask(27L, 55L, new APITask(55L, 8624L, "changed", 4, 2));
			fail("expecting exception");

		} catch (ResponseStatusException e) {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).contains("In order to perform this operation, the row id of the currently saved task must match the value passed in the url for rowId.");
		}
	}

	@Test
	void putTaskDoesNotAcceptATaskWithoutTitle() {
		APITask task = new APITask(1L,5L, null, 4, 3);

		try {
			api.putTask(5L, 1L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide a title in the request body.  An empty string is valid.\"");
		}
	}

	@Test
	void putTaskDoesNotAcceptATaskWithoutAPositiveSize() {

		try {
			api.putTask(5L, 1L, new APITask(1L, 5L,"", null, 3));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide a positive integer for size.\"");
		}

		try {
			api.putTask(5L, 1L, new APITask(1L, 5L,"", 0, 3));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide a positive integer for size.\"");
		}

		try {
			api.putTask(5L, 1L, new APITask(1L, 5L,"", -1, 3));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide a positive integer for size.\"");
		}
	}
	@Test
	void putTaskDoesNotAcceptATaskWithoutAPositivePosition() {

		try {
			api.putTask(5L, 1L, new APITask(1L, 5L,"", 3, null));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide a positive integer for position.\"");
		}

		try {
			api.putTask(5L, 1L, new APITask(1L, 5L,"", 3, 0));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide a positive integer for position.\"");
		}

		try {
			api.putTask(5L, 1L, new APITask(1L, 5L,"", 3, -1));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, make sure to provide a positive integer for position.\"");
		}
	}

	@Test
	void putTaskThrows404StyleExceptionIfDoesNotExist() {
		Mockito.when(taskService.findTaskWithId(1L)).thenThrow(new TaskService.TaskNotFoundException("st"));

		try {
			api.putTask(5L, 1L, new APITask(1L,5L, "something", 4, 3));
			fail("expecting exception");
		} catch (ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(404);
			assertThat(e.getMessage()).contains("Cannot PUT the task.  No task exists with the passed id: 1");
		}
	}

	@Test
	void putTaskThrows500StyleExceptionIfServiceThrowsUnexpectedException() {
		Mockito.when(taskService.findTaskWithId(55L)).thenReturn(new Task(1L, 5L, "st", 1, 2));

		APITask task = new APITask(1L, 5L, "something", 4, 2);
		Mockito.when(taskService.updateTask(task.toDomainObject())).thenThrow(new RuntimeException("st"));

		try {
			api.putTask(5L, 1L, task);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(500);
			assertThat(e.getMessage()).contains("Unexpected error encountered while attempting to update task with id 1.  Please try again, until a 200 message is returned.");
		}
	}

	@Test
	void putTaskDoesNotAcceptMismatchedIdsInUrlAndBody() {
		APITask task = new APITask(5L, 47L, "something", 4, 2);

		try {
			api.putTask(47L,7L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PUTing a Task, the url id and request body id must match.\"");
		}
	}

	@Test
	void getTasksRetrievesAllTasksFromServiceAndIncludesIds() {
		Task task1 = new Task(5L, 33L, "st", 4, 2);
		Task task2 = new Task(6L, 33L,"st else", 7, 3);
		List<Task> taskList = Arrays.asList(task1, task2);
		Mockito.when(taskService.getAllTasksForRow(33L)).thenReturn(taskList);

		List<APITask> apiTasks = api.getTasks(33L);

		assertThat(apiTasks.size()).isEqualTo(2);
		assertThat(apiTasks.get(0)).isEqualTo(new APITask(5L, 33L, "st", 4, 2));
		assertThat(apiTasks.get(1)).isEqualTo(new APITask(6L, 33L, "st else", 7, 3));
	}

	@Test
	void getTasksReturnsAnEmptyListIfNoTasksExist() {
		List<Task> taskList = new LinkedList<>();
		Mockito.when(taskService.getAllTasksForRow(5L)).thenReturn(taskList);

		List<APITask> apiTasks = api.getTasks(5L);

		assertThat(apiTasks).isNotNull();
		assertThat(apiTasks.size()).isEqualTo(0);
	}

	@Test
	void deleteTaskCallsServiceDeleteAndReturnsDeletedTask() {
		Mockito.when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 33L, "st", 7, 1));
		Mockito.when(taskService.deleteTask(55L)).thenReturn(new Task(55L, 33L,"st", 7, 1));

		APITask deletedTask = api.deleteTask(33L, 55L);

		assertThat(deletedTask.getId()).isEqualTo(55L);
		assertThat(deletedTask.getTitle()).isEqualTo("st");
		assertThat(deletedTask.getSize()).isEqualTo(7);
	}

	@Test
	void deleteTaskThrows404StyleExceptionIfServiceThrowsTaskNotFoundException() {
		Mockito.when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 5L, "st", 1, 7));
		Mockito.when(taskService.deleteTask(55L)).thenThrow(new TaskService.TaskNotFoundException("message should be independent"));

		try {
			api.deleteTask(5L, 55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(404);
			assertThat(e.getMessage()).contains("Task with id 55 does not exist.");
		}
	}

	@Test
	void deleteTaskThrows500StyleExceptionIfServiceThrowsUnexpectedException() {
		Mockito.when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 5L, "st", 1, 5));
		Mockito.when(taskService.deleteTask(55L)).thenThrow(new RuntimeException("st"));

		try {
			api.deleteTask(5L,55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(500);
			assertThat(e.getMessage()).contains("Unexpected error encountered while attempting to delete task with id 55.  Please try again, until a 404 message is returned.");
		}
	}

	@Test
	void deleteTaskRequiresPathRowIdToMatchWhatTheTaskHasSavedAsTheRowId() {
		Mockito.when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 1L, "st", 1, 4));

		try {
			api.deleteTask(5L,55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(400);
			assertThat(e.getMessage()).contains("In order to perform this operation, the row id of the currently saved task must match the value passed in the url for rowId.");
		}
	}

	@Test
	void deleteTaskThrows500StyleExceptionIfServiceThrowsUnexpectedExceptionDuringGet() {
		Mockito.when(taskService.findTaskWithId(55L)).thenThrow(new RuntimeException("st"));

		try {
			api.deleteTask(5L,55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getRawStatusCode()).isEqualTo(500);
			assertThat(e.getMessage()).contains("Unexpected error encountered while attempting to delete task with id 55.  Please try again, until a 404 message is returned.");
		}
	}
}
