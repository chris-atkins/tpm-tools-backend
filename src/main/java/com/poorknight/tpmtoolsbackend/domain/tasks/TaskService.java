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
		throwExceptionIfInvalidTaskForSaving(task);
		return repository.save(task);
	}

	private void throwExceptionIfInvalidTaskForSaving(Task task) {
		if (task.getId() != null) {
			throw new RuntimeException("Cannot specify an ID on a new Task!  Try the updateTask method instead :)");
		}
		if (task.getRowId() == null) {
			throw new RuntimeException("Must specify a rowId for a new Task.  No action taken.");
		}
		if (task.getTitle() == null) {
			throw new RuntimeException("Must specify a title for a new Task. An empty string is ok, null is not.  No action taken.");
		}
		if (task.getSize() == null) {
			throw new RuntimeException("Must specify a size for a new Task.  No action taken.");
		}
	}

	public Task updateTask(Task taskToUpdate) {
		throwExceptionIfInvalidTaskForUpdating(taskToUpdate);
		return repository.save(taskToUpdate);
	}

	private void throwExceptionIfInvalidTaskForUpdating(Task taskToUpdate) {
		if (taskToUpdate.getId() == null) {
			throw new RuntimeException("Must specify an ID to update a Task - that is how we know what Task to update! Try the saveNewTask method instead :)");
		}
		if (taskToUpdate.getRowId() == null) {
			throw new RuntimeException("Must specify a rowId when updating a Task.");
		}
		if (taskToUpdate.getTitle() == null) {
			throw new RuntimeException("Must specify a title while updating a Task. A full task must be given, including fields that are not changing.");
		}
		if (taskToUpdate.getSize() == null) {
			throw new RuntimeException("Must specify a size while updating a Task. A full task must be given, including fields that are not changing.");
		}

		Optional<Task> task = repository.findById(taskToUpdate.getId());
		if (task.isEmpty()) {
			throw new TaskNotFoundException("Cannot update task with id " + taskToUpdate.getId() + ". It does not exist.");
		}
	}

	public List<Task> getAllTasksForRow(Long rowId) {
		Iterable<Task> allTasks = repository.findAllTasksForRow(rowId);

		List<Task> taskList = new ArrayList<>();
		for (Task task : allTasks) {
			taskList.add(task);
		}
		return taskList;
	}

	public Task deleteTask(Long taskId) {
		Optional<Task> task = repository.findById(taskId);
		if (task.isEmpty()) {
			throw new TaskNotFoundException("Cannot delete task with id " + taskId + ". It does not exist.");
		}
		repository.deleteById(taskId);
		return task.get();
	}

	public Task findTaskWithId(Long taskId) {
		Optional<Task> task = repository.findById(taskId);
		if (task.isEmpty()) {
			throw new TaskNotFoundException("Cannot find task with id " + taskId + ".");
		}
		return task.get();
	}

	public static class TaskNotFoundException extends RuntimeException {

		public TaskNotFoundException(String message) {
			super(message);
		}
	}
}
