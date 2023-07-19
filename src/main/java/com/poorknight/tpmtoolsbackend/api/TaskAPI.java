package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.APITask;
import com.poorknight.tpmtoolsbackend.api.entity.APITaskPatch;
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
@RequestMapping("/api/v1/project-plans/{projectPlanId}/rows/{rowId}/tasks")
public class TaskAPI {

	@Autowired
	private TaskService taskService;

	@GetMapping
	public List<APITask> getTasks(@PathVariable Long rowId) {
		List<Task> allTasks = taskService.getAllTasksForRow(rowId);

		List<APITask> responseTasks = new ArrayList<>(allTasks.size());
		for (Task task: allTasks) {
			responseTasks.add(APITask.fromDomainObject(task));
		}
		return responseTasks;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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


	@PatchMapping(value = "/{taskId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APITask patchTask(@PathVariable long taskId, @RequestBody APITaskPatch patchBody) {
		validateTaskToPatchThrowingExceptions(taskId, patchBody);

		try {
			Task taskToUpdate = patchBody.toDomainObject();
			Task updatedTask = taskService.patchTask(taskToUpdate);
			return APITask.fromDomainObject(updatedTask);

		} catch (TaskService.TaskNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Cannot PATCH the task.  No task exists with the passed id: " + taskId);

		} catch (ResponseStatusException e) {
			throw e;

		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Unexpected error encountered while attempting to update task with id " + taskId + ".  Please try again, until a 200 message is returned.");
		}
	}

	private void validateTaskToPatchThrowingExceptions(Long taskId, APITaskPatch taskPatch) {
		if (taskPatch.getId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PATCHing a Task, make sure to provide an id in the request body.");
		}
		if (!taskPatch.getId().equals(taskId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PATCHing a Task, the url id and request body id must match.");
		}
		if (taskPatch.getTitle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When PATCHing a Task, make sure to provide a title in the request body.  An empty string is valid.");
		}
	}


	@DeleteMapping(value = "/{taskId}")
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
