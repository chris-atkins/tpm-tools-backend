package com.poorknight.tpmtoolsbackend.domain.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TaskService {

	@Autowired
	private TaskRepository repository;

	public Task saveNewTask(Task task) {
		if (task.getId() != null) {
			throw new RuntimeException("Cannot specify an ID on a new Task!  Try the updateTask method instead :)");
		}
		return repository.save(task);
	}

	public Task updateTask(Task taskToUpdate) {
		if (taskToUpdate.getId() == null) {
			throw new RuntimeException("Must specify an ID to update a Task - that is how we know what Task to update! Try the saveNewTask method instead :)");
		}
		Optional<Task> task = this.getTask(taskToUpdate.getId());
		if (task.isEmpty()) {
			throw new TaskNotFoundException("Cannot update task with id " + taskToUpdate.getId() + ". It does not exist.");
		}
		return repository.save(taskToUpdate);
	}

	public List<Task> getAllTasks() {
		Iterable<Task> allTasks = repository.findAll();

		List<Task> taskList = new ArrayList<>();
		for (Task task : allTasks) {
			taskList.add(task);
		}
		return taskList;
	}

	public Task deleteTask(Long taskId) {
		Optional<Task> task = this.getTask(taskId);
		if (task.isEmpty()) {
			throw new TaskNotFoundException("Cannot delete task with id " + taskId + ". It does not exist.");
		}
		repository.deleteById(taskId);
		return task.get();
	}

	private Optional<Task> getTask(Long taskId) {
		return repository.findById(taskId);
	}

	public static class TaskNotFoundException extends RuntimeException {

		public TaskNotFoundException(String message) {
			super(message);
		}
	}
}
