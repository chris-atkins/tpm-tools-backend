package com.poorknight.tpmtoolsbackend.domain.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
}
