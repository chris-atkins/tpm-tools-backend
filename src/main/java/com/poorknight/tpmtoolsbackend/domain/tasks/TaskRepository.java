package com.poorknight.tpmtoolsbackend.domain.tasks;

import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

public interface TaskRepository extends CrudRepository<Task, Long> {

	@Query("SELECT t FROM Task t WHERE t.rowId = ?1")
	Collection<Task> findAllTasksForRow(Long rowId);
}
