package com.poorknight.tpmtoolsbackend.domain.tasks;

import org.springframework.data.repository.CrudRepository;

/*package private*/ interface TaskRepository extends CrudRepository<Task, Long> {
	//empty for spring magic
}
