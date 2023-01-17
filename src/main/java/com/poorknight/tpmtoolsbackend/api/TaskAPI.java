package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.response.SimpleTask;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TaskAPI {

	@Autowired
	private TaskService taskService;

	@PostMapping(value = "/task", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public SimpleTask postTask(@RequestBody SimpleTask simpleTask) {
		validateTaskToPostThrowingExceptions(simpleTask);

		Task taskToSave = simpleTask.toDomainObject();
		Task savedTask = taskService.saveNewTask(taskToSave);
		return SimpleTask.fromDomainObject(savedTask);
	}

	private void validateTaskToPostThrowingExceptions(SimpleTask simpleTask) {
		if (simpleTask.getId() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"When POSTing a new Task, do not provide an id.  Maybe you intended to use a PUT.");
		}
	}

	@GetMapping(value = "/task")
	public List<SimpleTask> getTasks() {
		List<Task> allTasks = taskService.getAllTasks();

		List<SimpleTask> responseTasks = new ArrayList<>(allTasks.size());
		for (Task task: allTasks) {
			responseTasks.add(SimpleTask.fromDomainObject(task));
		}
		return responseTasks;
	}
}