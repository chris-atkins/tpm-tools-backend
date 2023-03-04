package com.poorknight.tpmtoolsbackend.domain.row;

import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class RowService {

	@Autowired
	private RowRepository rowRepository;

	@Autowired
	private TaskService taskService;

	public Row saveNewRow(Row newRow) {
		validateRowToSaveThrowingExceptions(newRow);
		return rowRepository.save(newRow);
	}

	private void validateRowToSaveThrowingExceptions(Row newRow) {
		if (newRow.getId() != null) {
			throw new RuntimeException("New Row cannot be saved with an id.  This is auto-assigned by the DB.  Maybe you would like to use an update operation.");
		}
		if (newRow.getTaskList() != null && newRow.getTaskList().size() > 0 ) {
			throw new RuntimeException("New Row cannot be saved with any tasks.  First save a row, then add tasks to it by saving individual tasks with a reference to the rowId.  Thanks!");
		}
	}

	public List<Row> getAllRows() {
		Iterable<Row> repositoryResults = rowRepository.findAll();
		List<Row> results = new LinkedList<>();
		for (Row row : repositoryResults) {
			results.add(row);
		}
		return results;
	}

	public Row updateRow(Row rowToUpdate) {
		validateRowToUpdateThrowingExceptions(rowToUpdate);
		return rowRepository.save(rowToUpdate);
	}

	private void validateRowToUpdateThrowingExceptions(Row rowToUpdate) {
		if (rowToUpdate.getTaskList() != null && rowToUpdate.getTaskList().size() > 0) {
			throw new RuntimeException("New Row cannot be updated with any tasks.  No updates to any tasks will be made through this operation - throwing an exception is to avoid any false impression that tasks might be updated.  Please pass an empty list, and make any task changes by updating the tasks themselves. Thanks!");
		}
		if (rowToUpdate.getId() == null) {
			throw new RuntimeException("Cannot update a row that does not have an id specified.  Maybe you meant to save a new row, instead of an update?");
		}
		if (rowRepository.findById(rowToUpdate.getId()).isEmpty()) {
			throw new RuntimeException("The rowId passed does not exist!  It is impossible to perform an update on a row that does not exist.");
		}
	}
}
