package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.APITask;
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

	@GetMapping(value = "/rows/{rowId}/tasks")
	public List<APITask> getTasks(@PathVariable Long rowId) {
		List<Task> allTasks = taskService.getAllTasksForRow(rowId);

		List<APITask> responseTasks = new ArrayList<>(allTasks.size());
		for (Task task: allTasks) {
			responseTasks.add(APITask.fromDomainObject(task));
		}
		return responseTasks;
	}

	@PostMapping(value = "/rows/{rowId}/tasks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APITask postTask(@PathVariable Long rowId, @RequestBody APITask task) {
		validateTaskToPostThrowingExceptions(rowId, task);

		Task taskToSave = task.toDomainObject();
		Task savedTask = taskService.saveNewTask(taskToSave);
		return task.fromDomainObject(savedTask);

	}

	private void validateTaskToPostThrowingExceptions(Long rowId, APITask task) {
		if (task.getId() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When POSTing a new Task, do not provide an id.  Maybe you intended to use a PUT.");
		}
		if (task.getRowId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When POSTing a new Task, a rowId is mandatory.");
		}
		if (!task.getRowId().equals(rowId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When POSTing a new Task, the rowId in the body must match the one in the url.");
		}
		if (task.getSize() == null || task.getSize() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When POSTing a new Task, a positive integer for size is mandatory.");
		}
		if (task.getPosition() == null || task.getPosition() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When POSTing a new Task, a positive integer for position is mandatory.");
		}
		if (task.getTitle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When POSTing a new Task, a title is mandatory.  An empty string is valid.");
		}
	}


	@PutMapping(value = "/rows/{rowId}/tasks/{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APITask putTask(@PathVariable Long rowId, @PathVariable long taskId, @RequestBody APITask taskBody) {
		validateTaskToPutThrowingExceptions(taskId, taskBody);

		try {
			checkTaskIdBelongsToRowIDInURLThrowingException(rowId, taskId);

			Task taskToUpdate = taskBody.toDomainObject();
			Task updatedTask = taskService.updateTask(taskToUpdate);
			return APITask.fromDomainObject(updatedTask);

		} catch (TaskService.TaskNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Cannot PUT the task.  No task exists with the passed id: " + taskId);

		} catch (ResponseStatusException e) {
			throw e;

		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Unexpected error encountered while attempting to update task with id " + taskId + ".  Please try again, until a 200 message is returned.");
		}
	}

	private void validateTaskToPutThrowingExceptions(Long taskId, APITask task) {
		if (task.getId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PUTing a Task, make sure to provide an id in the request body.");
		}
		if (task.getRowId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PUTing a Task, make sure to provide a rowId.");
		}
		if (task.getId() != taskId) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PUTing a Task, the url id and request body id must match.");
		}
		if (task.getTitle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PUTing a Task, make sure to provide a title in the request body.  An empty string is valid.");
		}
		if (task.getSize() == null || task.getSize() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PUTing a Task, make sure to provide a positive integer for size.");
		}
		if (task.getPosition() == null || task.getPosition() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PUTing a Task, make sure to provide a positive integer for position.");
		}
	}


	@DeleteMapping(value = "/rows/{rowId}/tasks/{taskId}")
	public APITask deleteTask(@PathVariable Long rowId, @PathVariable Long taskId) {
		try {
			checkTaskIdBelongsToRowIDInURLThrowingException(rowId, taskId);

			Task deletedTask = taskService.deleteTask(taskId);
			return APITask.fromDomainObject(deletedTask);

		} catch (TaskService.TaskNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Task with id " + taskId + " does not exist.");

		} catch (ResponseStatusException e) {
			throw e;

		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Unexpected error encountered while attempting to delete task with id " + taskId + ".  Please try again, until a 404 message is returned.");
		}
	}

	private void checkTaskIdBelongsToRowIDInURLThrowingException(Long rowId, Long taskId) {
		Task task = taskService.findTaskWithId(taskId);
		if (!task.getRowId().equals(rowId)){
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"In order to perform this operation, the row id of the currently saved task must match the value passed in the url for rowId.");
		}
	}
}
