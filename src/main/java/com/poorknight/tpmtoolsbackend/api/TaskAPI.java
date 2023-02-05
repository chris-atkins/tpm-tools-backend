package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.response.APITask;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TaskAPI {

	@Autowired
	private TaskService taskService;

	@GetMapping(value = "/task")
	public List<APITask> getTasks() {
		List<Task> allTasks = taskService.getAllTasks();

		List<APITask> responseTasks = new ArrayList<>(allTasks.size());
		for (Task task: allTasks) {
			responseTasks.add(APITask.fromDomainObject(task));
		}
		return responseTasks;
	}

	@PostMapping(value = "/task", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APITask postTask(@RequestBody APITask task) {
		validateTaskToPostThrowingExceptions(task);

		Task taskToSave = task.toDomainObject();
		Task savedTask = taskService.saveNewTask(taskToSave);
		return task.fromDomainObject(savedTask);

	}

	private void validateTaskToPostThrowingExceptions(APITask task) {
		if (task.getId() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When POSTing a new Task, do not provide an id.  Maybe you intended to use a PUT.");
		}
	}


	@PutMapping(value = "/task/{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APITask putTask(@PathVariable long taskId, @RequestBody APITask taskBody) {
		validateTaskToPutThrowingExceptions(taskId, taskBody);

		try {
			Task taskToUpdate = taskBody.toDomainObject();
			Task updatedTask = taskService.updateTask(taskToUpdate);
			return APITask.fromDomainObject(updatedTask);

		} catch (TaskService.TaskNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot PUT the task.  No task exists with the passed id: " + taskId);

		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error encountered while attempting to update task with id " + taskId + ".  Please try again, until a 200 message is returned.");
		}
	}

	private void validateTaskToPutThrowingExceptions(long taskId, APITask task) {
		if (task.getId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PUTing a Task, make sure to provide an id in the request body.");
		}
		if (task.getId() != taskId) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PUTing a Task, the url id and request body id must match.");
		}
	}

	@DeleteMapping(value = "/task/{taskId}")
	public APITask deleteTask(@PathVariable Long taskId) {
		try {
			Task deletedTask = taskService.deleteTask(taskId);
			return APITask.fromDomainObject(deletedTask);

		} catch (TaskService.TaskNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with id " + taskId + " does not exist.");

		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error encountered while attempting to delete task with id " + taskId + ".  Please try again, until a 404 message is returned.");
		}
	}
}
