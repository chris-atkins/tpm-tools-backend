package com.poorknight.tpmtoolsbackend.domain.projectplan;

import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectConsistencyValidator.RowUpdateConsistencyException;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlanPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.TaskPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectConsistencyValidator.*;
import static org.assertj.core.api.Assertions.*;
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
				TaskPatchTemplate.builder().id(11L).size(1).position(3).build()
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
				TaskPatchTemplate.builder().id(11L).size(1).position(0).build()
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
				TaskPatchTemplate.builder().id(10L).size(1).position(5).build(),
				TaskPatchTemplate.builder().id(11L).size(1).position(0).build()
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
				TaskPatchTemplate.builder().id(12L).size(1).position(3).build()
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
				TaskPatchTemplate.builder().id(10L).size(5).position(1).build()
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
				TaskPatchTemplate.builder().id(10L).size(4).position(3).build(),
				TaskPatchTemplate.builder().id(11L).size(3).position(15).build(),
				TaskPatchTemplate.builder().id(12L).size(5).position(7).build()
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
				TaskPatchTemplate.builder().id(12L).size(1).position(0).build(),
				TaskPatchTemplate.builder().id(10L).size(null).position(4).build(),
				TaskPatchTemplate.builder().id(11L).size(3).position(null).build()
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
				TaskPatchTemplate.builder().id(10L).size(null).position(1).build(),
				TaskPatchTemplate.builder().id(11L).size(2).position(null).build()
		));

		try {
			new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);
			fail("expecting exception");
		} catch (RowUpdateConsistencyException e) {
			assertThat(e.getMessage()).contains("The proposed change results in more than one task occupying the same space.");
		}
	}

	@Test
	void throwsExceptionWhenTaskDoesNotExist() {
		Row row = new Row(1L, 55L, "row title", List.of(
				new Task(10L, 1L, "task 1", 1, 0),
				new Task(11L, 1L, "task 2", 1, 1),
				new Task(12L, 1L, "task 3", 1, 2)
		));

		RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, "row title", List.of(
				TaskPatchTemplate.builder().id(13L).size(1).position(null).build()
		));

		try {
			new ProjectConsistencyValidator().validateRowChangeSetThrowingExceptions(row, rowPatchTemplate);
			fail("expecting exception");
		} catch (RuntimeException e) {
			assertThat(e.getClass()).isEqualTo(RowUpdateConsistencyException.class);
			assertThat(e.getMessage()).contains("The patch request refers to a task ID that does not exist: 13");
		}
	}

	@Test
	void wholeProjectValidatorPassesWithValidChangeMovingATaskToANewRow() {
		Row row1 = new Row(1L, 55L, "row 1", List.of(
				new Task(10L, 1L, "task 1-1", 1, 0),
				new Task(11L, 1L, "task 1-2", 2, 1),
				new Task(12L, 1L, "task 1-3", 1, 3)
		));

		Row row2 = new Row(2L, 55L, "row 2", List.of(
				new Task(20L, 2L, "task 2-1", 1, 0),
				new Task(21L, 2L, "task 2-2", 1, 1),
				new Task(22L, 2L, "task 2-3", 1, 2)
		));

		ProjectPlan projectPlan = new ProjectPlan(55L, "", List.of(row1, row2));


		RowPatchTemplate row2PatchTemplate = new RowPatchTemplate(2L, null, List.of(
				TaskPatchTemplate.builder().id(11L).rowId(2L).position(1).build(),
				TaskPatchTemplate.builder().id(21L).position(3).build(),
				TaskPatchTemplate.builder().id(22L).position(4).build()
		));
		ProjectPlanPatchTemplate projectPlanPatchTemplate = new ProjectPlanPatchTemplate(55L, "", List.of(row2PatchTemplate));


		new ProjectConsistencyValidator().validateProjectPlanChangeSetThrowingExceptions(projectPlan, projectPlanPatchTemplate);

		assertThat(true).isTrue(); // validation should pass - nothing happens other than no exceptions are thrown
	}

	@Test
	void wholeProjectValidatorFailsIfMovingATaskToANewRowResultsInOverlap() {
		Row row1 = new Row(1L, 55L, "row 1", List.of(
				new Task(10L, 1L, "task 1-1", 1, 0),
				new Task(11L, 1L, "task 1-2", 2, 1),
				new Task(12L, 1L, "task 1-3", 1, 3)
		));

		Row row2 = new Row(2L, 55L, "row 2", List.of(
				new Task(20L, 1L, "task 2-1", 1, 0),
				new Task(21L, 1L, "task 2-2", 1, 1),
				new Task(22L, 1L, "task 2-3", 1, 2)
		));

		ProjectPlan projectPlan = new ProjectPlan(55L, "", List.of(row1, row2));


		RowPatchTemplate row2PatchTemplate = new RowPatchTemplate(2L, null, List.of(
				TaskPatchTemplate.builder().id(11L).rowId(2L).position(1).build(),
				TaskPatchTemplate.builder().id(21L).position(2).build()
		));
		ProjectPlanPatchTemplate projectPlanPatchTemplate = new ProjectPlanPatchTemplate(55L, "", List.of(row2PatchTemplate));


		assertThatThrownBy(() ->
				new ProjectConsistencyValidator().validateProjectPlanChangeSetThrowingExceptions(projectPlan, projectPlanPatchTemplate))
				.isOfAnyClassIn(ProjectPlanUpdateConsistencyException.class)
				.hasMessage("The proposed change results in more than one task occupying the same space.");
	}

	@Test
	void passesValidationIfNullRowsInProjectPlanTemplate() {
		Row row1 = new Row(1L, 55L, "row 1", List.of(
				new Task(10L, 1L, "task 1-1", 1, 0),
				new Task(11L, 1L, "task 1-2", 2, 1),
				new Task(12L, 1L, "task 1-3", 1, 3)
		));

		Row row2 = new Row(2L, 55L, "row 2", List.of(
				new Task(20L, 2L, "task 2-1", 1, 0),
				new Task(21L, 2L, "task 2-2", 1, 1),
				new Task(22L, 2L, "task 2-3", 1, 2)
		));

		ProjectPlan projectPlan = new ProjectPlan(55L, "", List.of(row1, row2));

		ProjectPlanPatchTemplate projectPlanPatchTemplate = new ProjectPlanPatchTemplate(55L, "new title", null);
		new ProjectConsistencyValidator().validateProjectPlanChangeSetThrowingExceptions(projectPlan, projectPlanPatchTemplate);

		assertThat(true).isTrue(); // validation should pass - nothing happens other than no exceptions are thrown
	}
}