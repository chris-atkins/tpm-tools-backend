package com.poorknight.tpmtoolsbackend.domain.row;

import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;

import java.util.Optional;

public class RowServiceValidator {

	static protected void validateRowToSaveThrowingExceptions(Row newRow) {
		if (newRow.getId() != null) {
			throw new RuntimeException("New Row cannot be saved with an id.  This is auto-assigned by the DB.  Maybe you would like to use an update operation.");
		}
		if (newRow.getTaskList() != null && newRow.getTaskList().size() > 0 ) {
			throw new RuntimeException("New Row cannot be saved with any tasks.  First save a row, then add tasks to it by saving individual tasks with a reference to the rowId.  Thanks!");
		}
	}

	static protected void validateRowPatch(RowPatchTemplate rowPatchTemplate, Optional<Row> optionalBeingUpdated) {
		if (rowPatchTemplate.getId() == null) {
			throw new RuntimeException("Cannot update a row that does not have an id specified.  Maybe you meant to save a new row, instead of an update?");
		}

		if (optionalBeingUpdated.isEmpty()) {
			throw new RowNotFoundException("The rowId passed does not exist!  It is impossible to perform an update on a row that does not exist.");
		}
	}

	static protected void validateRowDelete(Long rowId, Optional<Row> maybeRow) {
		if(maybeRow.isEmpty()) {
			throw new RowNotFoundException("The rowId passed does not point to a valid row.  No changes were made.");
		}

		Row row = maybeRow.get();

		if (!row.getTaskList().isEmpty()) {
			throw new CannotDeleteNonEmptyRowException("Cannot delete a row that has tasks that belong to it.  Please delete the tasks or move them to another row before deleting this row.");
		}
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
