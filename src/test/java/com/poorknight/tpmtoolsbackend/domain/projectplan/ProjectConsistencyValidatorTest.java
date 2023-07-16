package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectConsistencyValidator.RowUpdateConsistencyException;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplateTask;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class ProjectConsistencyValidatorTest {

	@Test
	void noExceptionIfTheUpdateWouldResultInAValidNewRow() {
		Row row = new Row(1L, 55L, "row title", List.of(
				new Task(10L, 1L, "task 1", 1, 0),
				new Task(11L, 1L, "task 2", 1, 1),
				new Task(12L, 1L, "task 3", 1, 2)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				new RowPatchTemplateTask(11L, 1, 3)
		));

		new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);

		assertThat(true).isTrue(); // validation should pass - nothing happens other than no exceptions are thrown
	}

	@Test
	void validateRowUpdateThrowsExceptionIfSizeAndPositionOverlapForSimplestChange() {
		Row row = new Row(1L, 55L, "row title", List.of(
						new Task(10L, 1L, "task 1", 1, 0),
						new Task(11L, 1L, "task 2", 1, 1),
						new Task(12L, 1L, "task 3", 1, 2)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				new RowPatchTemplateTask(11L, 1, 0)
		));

		try {
			new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);
			fail("expecting exception");
		} catch (RowUpdateConsistencyException e) {
			assertThat(e.getMessage()).contains("The proposed change results in more than one task occupying the same space.");
		}
	}

	@Test
	void validateRowUpdatePassesIfSizeAndPositionOverlapWhereChangeClearsPreviouslyOccupiedSpace() {
		Row row = new Row(1L, 55L, "row title", List.of(
						new Task(10L, 1L, "task 1", 1, 0),
						new Task(11L, 1L, "task 2", 1, 1),
						new Task(12L, 1L, "task 3", 1, 2)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				new RowPatchTemplateTask(10L, 1, 5),
				new RowPatchTemplateTask(11L, 1, 0)
		));

		new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);

		assertThat(true).isTrue(); // validation should pass - nothing happens other than no exceptions are thrown
	}

	@Test
	void validateRowUpdateThrowsExceptionIfSizeAndPositionOverlapWithLargeTasks() {
		Row row = new Row(1L, 55L, "row title", List.of(
				new Task(10L, 1L, "task 1", 5, 0),
				new Task(11L, 1L, "task 2", 1, 5),
				new Task(12L, 1L, "task 3", 1, 6)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				new RowPatchTemplateTask(12L, 1, 3)
		));

		try {
			new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);
			fail("expecting exception");
		} catch (RowUpdateConsistencyException e) {
			assertThat(e.getMessage()).contains("The proposed change results in more than one task occupying the same space.");
		}
	}

	@Test
	void validateRowUpdateThrowsExceptionIfSizeAndPositionOverlapWithLargeChangingTasks() {
		Row row = new Row(1L, 55L, "row title", List.of(
				new Task(10L, 1L, "task 1", 5, 0),
				new Task(11L, 1L, "task 2", 1, 5),
				new Task(12L, 1L, "task 3", 1, 6)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				new RowPatchTemplateTask(10L, 5, 1)
		));

		try {
			new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);
			fail("expecting exception");
		} catch (RowUpdateConsistencyException e) {
			assertThat(e.getMessage()).contains("The proposed change results in more than one task occupying the same space.");
		}
	}

	@Test
	void validateRowUpdatePassesIfSizeAndPositionOverlapTotallyChangingEverything() {
		Row row = new Row(1L, 55L, "row title", List.of(
				new Task(10L, 1L, "task 1", 1, 0),
				new Task(11L, 1L, "task 2", 1, 1),
				new Task(12L, 1L, "task 3", 1, 2)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				new RowPatchTemplateTask(10L, 4, 3),
				new RowPatchTemplateTask(11L, 3, 15),
				new RowPatchTemplateTask(12L, 5, 7)
		));

		new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);

		assertThat(true).isTrue(); // validation should pass - nothing happens other than no exceptions are thrown
	}

	@Test
	void canHandleNullSizeOrPositionInPatchTemplateUsingOriginalValuesHappyCase() {
		Row row = new Row(1L, 55L, "row title", List.of(
				new Task(10L, 1L, "task 1", 1, 0),
				new Task(11L, 1L, "task 2", 1, 1),
				new Task(12L, 1L, "task 3", 1, 2)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				new RowPatchTemplateTask(12L, 1, 0),
				new RowPatchTemplateTask(10L, null, 4),
				new RowPatchTemplateTask(11L, 3, null)
		));

		new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);

		assertThat(true).isTrue(); // validation should pass - nothing happens other than no exceptions are thrown
	}

	@Test
	void canHandleNullSizeOrPositionInPatchTemplateUsingOriginalValuesSadCase() {
		Row row = new Row(1L, 55L, "row title", List.of(
				new Task(10L, 1L, "task 1", 5, 0),
				new Task(11L, 1L, "task 2", 1, 5),
				new Task(12L, 1L, "task 3", 1, 6)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				new RowPatchTemplateTask(10L, null, 1),
				new RowPatchTemplateTask(11L, 2, null)
		));

		try {
			new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);
			fail("expecting exception");
		} catch (RowUpdateConsistencyException e) {
			assertThat(e.getMessage()).contains("The proposed change results in more than one task occupying the same space.");
		}
	}
}