package com.poorknight.tpmtoolsbackend.domain.row;

import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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

	public Row updateRow(RowPatch rowToPatch) {
		validateRowToUpdateThrowingExceptions(rowToPatch);
		Optional<Row> existingRow = rowRepository.findById(rowToPatch.getId());

		if (existingRow.isEmpty()) {
			throw new RowNotFoundException("The rowId passed does not exist!  It is impossible to perform an update on a row that does not exist.");
		}
		Row updatedRow = existingRow.get();
		updatedRow.setTitle(rowToPatch.getTitle());

		return rowRepository.save(updatedRow);
	}

	private void validateRowToUpdateThrowingExceptions(RowPatch rowToUpdate) {
		if (rowToUpdate.getId() == null) {
			throw new RuntimeException("Cannot update a row that does not have an id specified.  Maybe you meant to save a new row, instead of an update?");
		}
	}

	public Row deleteEmptyRowById(Long rowId) {
		Row row = getRowToBeDeletedWhileValidatingWithExceptions(rowId);
		rowRepository.deleteById(rowId);
		return row;
	}

	private Row getRowToBeDeletedWhileValidatingWithExceptions(Long rowId) {
		Optional<Row> maybeRow = rowRepository.findById(rowId);

		if(maybeRow.isEmpty()) {
			throw new RowNotFoundException("The rowId passed does not point to a valid row.  No changes were made.");
		}

		Row row = maybeRow.get();

		if (!row.getTaskList().isEmpty()) {
			throw new CannotDeleteNonEmptyRowException("Cannot delete a row that has tasks that belong to it.  Please delete the tasks or move them to another row before deleting this row.");
		}
		return row;
	}


	public static class RowNotFoundException extends RuntimeException {

		public RowNotFoundException(String message) {
			super(message);
		}
	}

	public static class CannotDeleteNonEmptyRowException extends RuntimeException {
		public CannotDeleteNonEmptyRowException(String message) {
			super(message);
		}
	}
}
