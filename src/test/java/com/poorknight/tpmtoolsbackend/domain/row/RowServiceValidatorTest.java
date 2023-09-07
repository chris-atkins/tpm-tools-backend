package com.poorknight.tpmtoolsbackend.domain.row;

import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class RowServiceValidatorTest {

	private RowServiceValidator rowServiceValidator = new RowServiceValidator();

	// SAVE TESTS

	@Test
	void cannotSaveNewRowWithIdSpecified() {
		try {
			rowServiceValidator.validateRowToSaveThrowingExceptions(new Row(1L, 55L,"title", new ArrayList<>()));
			fail("Expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).contains("New Row cannot be saved with an id.  This is auto-assigned by the DB.  Maybe you would like to use an update operation.");
		}
	}

	@Test
	void cannotSaveNewRowWithTaskList() {
		try {
			List<Task> taskList = ImmutableList.<Task>builder().add(new Task(5L, "tilte", 1, 5)).build();
			Row newRow = new Row(null, 55L, "title", taskList);
			rowServiceValidator.validateRowToSaveThrowingExceptions(newRow);
			fail("Expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).contains("New Row cannot be saved with any tasks.  First save a row, then add tasks to it by saving individual tasks with a reference to the rowId.  Thanks!");
		}
	}

	// DELETE TESTS

	@Test
	void cannotDeleteARowThatHasTasksAssociatedWithIt() {
		Row rowBeingDeleted = new Row(1L, 55L, "title", List.of(new Task(2L, 1L, "task title", 1, 1)));

		try {
			rowServiceValidator.validateRowDelete(1L, Optional.of(rowBeingDeleted));
			fail("expecting exception");

		} catch (RowServiceValidator.CannotDeleteNonEmptyRowException e) {
			assertThat(e.getMessage()).isEqualTo("Cannot delete a row that has tasks that belong to it.  Please delete the tasks or move them to another row before deleting this row.");
		} catch (Exception e) {
			fail("expecting CannotDeleteNonEmptyRowException, instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void deleteErrorsIfThereIsNothingToDelete() {
		try {
			rowServiceValidator.validateRowDelete(1L, Optional.empty());
			fail("expecting exception");

		} catch (RowServiceValidator.RowNotFoundException e) {
			assertThat(e.getMessage()).isEqualTo("No row exists to be deleted.  No changes were made.");

		} catch (Exception e) {
			fail("expecting a RowNotFoundException - instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void deleteErrorsIfPassedIdDoesNotMatchTheRowsId() {
		Row rowBeingDeleted = new Row(1L, 55L, "title");

		try {
			rowServiceValidator.validateRowDelete(123L, Optional.of(rowBeingDeleted));
			fail("expecting exception");

		} catch (RowServiceValidator.MismatchedIdsException e) {
			assertThat(e.getMessage()).isEqualTo("The id does not match the proposed row to delete.  No changes were made.");

		} catch (Exception e) {
			fail("expecting a RowNotFoundException - instead got " + e.getClass().getCanonicalName());
		}
	}

	// UPDATE / PATCH TESTS

	@Test
	void updateDoesNotWorkOnARowThatDoesNotExist() {
		try {
			RowPatchTemplate rowParowPatchTemplate = new RowPatchTemplate(1L, "new title", null);

			rowServiceValidator.validateRowPatch(rowParowPatchTemplate, Optional.empty());
			fail("Expecting exception");

		} catch (RowServiceValidator.RowNotFoundException e) {
			assertThat(e.getMessage()).contains("No row exists to be updated!  It is impossible to perform an update on a row that does not exist.");

		} catch (RuntimeException e) {
			fail("expecting a RowNotFoundException - instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void updateDoesNotWorkOnMismatchedIds() {
		try {
			RowPatchTemplate rowParowPatchTemplate = new RowPatchTemplate(1L, "new title", null);

			rowServiceValidator.validateRowPatch(rowParowPatchTemplate, Optional.of(new Row(2L, 55L, "some title")));
			fail("Expecting exception");

		} catch (RowServiceValidator.MismatchedIdsException e) {
			assertThat(e.getMessage()).contains("The id does not match the proposed row to update.  No changes were made.");

		} catch (RuntimeException e) {
			fail("expecting a RowNotFoundException - instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void updateDoesNotWorkWithNullId() {
		try {
			RowPatchTemplate rowParowPatchTemplate = new RowPatchTemplate(null, "new title", null);

			rowServiceValidator.validateRowPatch(rowParowPatchTemplate, Optional.of(new Row(1L, 55L, "some title")));
			fail("Expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).contains("Cannot update a row that does not have an id specified.  Maybe you meant to save a new row, instead of an update?");
		}
	}


}