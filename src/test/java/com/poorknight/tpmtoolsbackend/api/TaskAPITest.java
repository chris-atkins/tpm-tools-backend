package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.response.SimpleTask;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

		SimpleTask response = api.postTask(new SimpleTask(null, taskTitle));

		assertThat(response.getTitle()).isEqualTo(taskTitle);
		assertThat(response.getId()).isEqualTo(idFromService);
	}

	@Test
	void postTaskDoesNotAcceptAnId() {
		SimpleTask task = new SimpleTask(3L, "something");

		try {
			api.postTask(task);
			Assertions.fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("400 BAD_REQUEST \"When POSTing a new Task, do not provide an id.  Maybe you intended to use a PUT.\"");
		}
	}

	@Test
	void getTasksRetrievesAllTasksFromServiceAndIncludesIds() {
		Task task1 = new Task(5L, "st");
		Task task2 = new Task(6L, "st else");
		List<Task> taskList = Arrays.asList(task1, task2);
		Mockito.when(taskService.getAllTasks()).thenReturn(taskList);

		List<SimpleTask> apiTasks = api.getTasks();

		assertThat(apiTasks.size()).isEqualTo(2);
		assertThat(apiTasks.get(0)).isEqualTo(new SimpleTask(5L, "st"));
		assertThat(apiTasks.get(1)).isEqualTo(new SimpleTask(6L, "st else"));
	}

	@Test
	void getTasksReturnsAnEmptyListIfNoTasksExist() {
		List<Task> taskList = new LinkedList<>();
		Mockito.when(taskService.getAllTasks()).thenReturn(taskList);

		List<SimpleTask> apiTasks = api.getTasks();

		assertThat(apiTasks).isNotNull();
		assertThat(apiTasks.size()).isEqualTo(0);
	}
}
