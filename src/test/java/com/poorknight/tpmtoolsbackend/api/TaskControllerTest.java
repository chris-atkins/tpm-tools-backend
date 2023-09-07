package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.APITask;
import com.poorknight.tpmtoolsbackend.api.entity.APITaskPatch;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

	@InjectMocks
	TaskController api;

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
		when(taskService.saveNewTask(inputTask)).thenReturn(savedTask);

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
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a non-negative integer for position is mandatory.\"");
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
	void postTaskRequiresANonNegativePosition() {
		when(taskService.saveNewTask(any())).thenReturn(new Task());
		APITask savedTask = api.postTask(55L, new APITask(null, 55L, "something", 5, 0));
		assertThat(savedTask).isNotNull(); // no error on zero position

		try {
			api.postTask(55L, new APITask(null, 55L, "something", 5, -1));
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, a non-negative integer for position is mandatory.\"");
		}
	}

	@Test
	void patchTaskReturnsResponseFromServiceAfterCallingUpdate() {
		Task inputTask = new Task(55L, null, "changed", null, null);
		Task savedTask = new Task(55L, 27L, "changed", 4, 3);
		when(taskService.patchTask(inputTask)).thenReturn(savedTask);

		APITask response = api.patchTask(55L, new APITaskPatch(55L, "changed"));

		assertThat(response.getId()).isEqualTo((Long)55L);
		assertThat(response.getRowId()).isEqualTo((Long)27L);
		assertThat(response.getTitle()).isEqualTo("changed");
		assertThat(response.getSize()).isEqualTo((Integer)4);
		assertThat(response.getPosition()).isEqualTo((Integer)3);
	}

	@Test
	void patchTaskDoesNotAcceptATaskWithoutId() {
		APITaskPatch task = new APITaskPatch(null, "something");

		try {
			api.patchTask(1L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PATCHing a Task, make sure to provide an id in the request body.\"");
		}
	}


	@Test
	void patchTaskDoesNotAcceptATaskWithoutTitle() {
		APITaskPatch task = new APITaskPatch(1L, null);

		try {
			api.patchTask(1L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PATCHing a Task, make sure to provide a title in the request body.  An empty string is valid.\"");
		}
	}

	@Test
	void patchTaskThrows404StyleExceptionIfDoesNotExist() {
		APITaskPatch patchBody = new APITaskPatch(1L, "something");
		when(taskService.patchTask(patchBody.toDomainObject())).thenThrow(new TaskService.TaskNotFoundException("st"));

		try {
			api.patchTask(1L, patchBody);
			fail("expecting exception");
		} catch (ResponseStatusException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(e.getMessage()).contains("Cannot PATCH the task.  No task exists with the passed id: 1");
		}
	}

	@Test
	void patchTaskThrows500StyleExceptionIfServiceThrowsUnexpectedException() {
		APITaskPatch task = new APITaskPatch(1L, "something");
		when(taskService.patchTask(task.toDomainObject())).thenThrow(new RuntimeException("st"));

		try {
			api.patchTask(1L, task);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getMessage()).contains("Unexpected error encountered while attempting to update task with id 1.  Please try again, until a 200 message is returned.");
		}
	}

	@Test
	void patchTaskDoesNotAcceptMismatchedIdsInUrlAndBody() {
		APITaskPatch task = new APITaskPatch(5L, "something");

		try {
			api.patchTask(7L, task);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When PATCHing a Task, the url id and request body id must match.\"");
		}
	}

	@Test
	void getTasksRetrievesAllTasksFromServiceAndIncludesIds() {
		Task task1 = new Task(5L, 33L, "st", 4, 2);
		Task task2 = new Task(6L, 33L,"st else", 7, 3);
		List<Task> taskList = List.of(task1, task2);
		when(taskService.getAllTasksForRow(33L)).thenReturn(taskList);

		List<APITask> apiTasks = api.getTasks(33L);

		assertThat(apiTasks.size()).isEqualTo(2);
		assertThat(apiTasks.get(0)).isEqualTo(new APITask(5L, 33L, "st", 4, 2));
		assertThat(apiTasks.get(1)).isEqualTo(new APITask(6L, 33L, "st else", 7, 3));
	}

	@Test
	void getTasksReturnsAnEmptyListIfNoTasksExist() {
		List<Task> taskList = new LinkedList<>();
		when(taskService.getAllTasksForRow(5L)).thenReturn(taskList);

		List<APITask> apiTasks = api.getTasks(5L);

		assertThat(apiTasks).isNotNull();
		assertThat(apiTasks.size()).isEqualTo(0);
	}

	@Test
	void deleteTaskCallsServiceDeleteAndReturnsDeletedTask() {
		when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 33L, "st", 7, 1));
		when(taskService.deleteTask(55L)).thenReturn(new Task(55L, 33L,"st", 7, 1));

		APITask deletedTask = api.deleteTask(33L, 55L);

		assertThat(deletedTask.getId()).isEqualTo(55L);
		assertThat(deletedTask.getTitle()).isEqualTo("st");
		assertThat(deletedTask.getSize()).isEqualTo(7);
	}

	@Test
	void deleteTaskThrows404StyleExceptionIfServiceThrowsTaskNotFoundException() {
		when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 5L, "st", 1, 7));
		when(taskService.deleteTask(55L)).thenThrow(new TaskService.TaskNotFoundException("message should be independent"));

		try {
			api.deleteTask(5L, 55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(e.getMessage()).contains("Task with id 55 does not exist.");
		}
	}

	@Test
	void deleteTaskThrows500StyleExceptionIfServiceThrowsUnexpectedException() {
		when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 5L, "st", 1, 5));
		when(taskService.deleteTask(55L)).thenThrow(new RuntimeException("st"));

		try {
			api.deleteTask(5L,55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getMessage()).contains("Unexpected error encountered while attempting to delete task with id 55.  Please try again, until a 404 message is returned.");
		}
	}

	@Test
	void deleteTaskRequiresPathRowIdToMatchWhatTheTaskHasSavedAsTheRowId() {
		when(taskService.findTaskWithId(55L)).thenReturn(new Task(55L, 1L, "st", 1, 4));

		try {
			api.deleteTask(5L,55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).contains("In order to perform this operation, the row id of the currently saved task must match the value passed in the url for rowId.");
		}
	}

	@Test
	void deleteTaskThrows500StyleExceptionIfServiceThrowsUnexpectedExceptionDuringGet() {
		when(taskService.findTaskWithId(55L)).thenThrow(new RuntimeException("st"));

		try {
			api.deleteTask(5L,55L);
			fail("expecting exception");
		} catch(ResponseStatusException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
			assertThat(e.getMessage()).contains("Unexpected error encountered while attempting to delete task with id 55.  Please try again, until a 404 message is returned.");
		}
	}
}
