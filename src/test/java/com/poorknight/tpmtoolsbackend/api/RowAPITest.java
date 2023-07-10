package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.response.APIRow;
import com.poorknight.tpmtoolsbackend.api.entity.response.APIRowPatch;
import com.poorknight.tpmtoolsbackend.api.entity.response.APITask;
import com.poorknight.tpmtoolsbackend.domain.row.Row;
import com.poorknight.tpmtoolsbackend.domain.row.RowService;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class RowAPITest {

	@InjectMocks
	private RowAPI api;

	@Mock
	private RowService rowService;

	@Test
	void getAllRowsReturnsResponseBasedOnServiceResults() {
		Task task1 = new Task(1L, 5L, "hi", 7, 4);
		Task task2 = new Task(2L, 5L, "oh", 8, 5);
		Row row1 = new Row(5L, "the real title", Arrays.asList(task1, task2));

		Row row2 = new Row(6L, "an imaginary title", new ArrayList<>());

		Mockito.when(rowService.getAllRows()).thenReturn(Arrays.asList(row1, row2));

		List<APIRow> response = api.getAllRows();

		assertThat(response.size()).isEqualTo(2);
		APIRow first = response.get(0);
		APIRow second = response.get(1);

		assertThat(first.getId()).isEqualTo(5L);
		assertThat(first.getTitle()).isEqualTo("the real title");
		assertThat(first.getTasks().size()).isEqualTo(2);

		APITask firstTask = first.getTasks().get(0);
		assertThat(firstTask.getId()).isEqualTo(1L);
		assertThat(firstTask.getRowId()).isEqualTo(5L);
		assertThat(firstTask.getTitle()).isEqualTo("hi");
		assertThat(firstTask.getSize()).isEqualTo(7);
		assertThat(firstTask.getPosition()).isEqualTo(4);

		APITask secondTask = first.getTasks().get(1);
		assertThat(secondTask.getId()).isEqualTo(2L);
		assertThat(secondTask.getRowId()).isEqualTo(5L);
		assertThat(secondTask.getTitle()).isEqualTo("oh");
		assertThat(secondTask.getSize()).isEqualTo(8);
		assertThat(secondTask.getPosition()).isEqualTo(5);

		assertThat(second.getId()).isEqualTo(6L);
		assertThat(second.getTitle()).isEqualTo("an imaginary title");
		assertThat(second.getTasks().size()).isEqualTo(0);
	}

	@Test
	void postNewRowCallsServiceAndReturnsResponseWithIdAdded() {
		Row expectedInput = new Row(null, "tittle", new ArrayList<>());
		Row responseFromService = new Row(1L, "tittle", new ArrayList<>());

		Mockito.when(rowService.saveNewRow(expectedInput)).thenReturn(responseFromService);

		APIRow row = new APIRow(null, "tittle", null);
		APIRow response = api.postNewRow(row);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("tittle");
		assertThat(response.getTasks().size()).isEqualTo(0);
	}

	@Test
	void postNewRowDoesNotAcceptTasksInTheRow() {
		try {
			api.postNewRow(new APIRow(null, "ohai", Arrays.asList(new APITask(1L, 2L, "hi", 3, 4))));
			fail("Expecting exception");
		} catch (ResponseStatusException e) {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).contains("Do not include tasks when saving a new Row.  Save the row with an empty task list, and then update each task with the row's id.");
		}
	}

	@Test
	void postNewRowDoesNotAcceptARowId() {
		try {
			api.postNewRow(new APIRow(1L, "ohai", null));
			fail("Expecting exception");
		} catch (ResponseStatusException e) {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).contains("Do not include an id when saving a new row.");
		}
	}

	@Test
	void postNewRowDoesAcceptEitherNullOrEmptyListOfTasks() {
		Row expectedInput = new Row(null, "tittle", new ArrayList<>());
		Row responseFromService = new Row(1L, "tittle", new ArrayList<>());

		Mockito.when(rowService.saveNewRow(expectedInput)).thenReturn(responseFromService);

		APIRow response1 = api.postNewRow(new APIRow(null, "tittle", null));

		assertThat(response1.getId()).isEqualTo(1L);
		assertThat(response1.getTitle()).isEqualTo("tittle");
		assertThat(response1.getTasks().size()).isEqualTo(0);

		APIRow response2 = api.postNewRow(new APIRow(null, "tittle", null));

		assertThat(response2.getId()).isEqualTo(1L);
		assertThat(response2.getTitle()).isEqualTo("tittle");
		assertThat(response2.getTasks().size()).isEqualTo(0);
	}

	@Test
	void patchRowReturnsResponseBasedOnServiceResultsIncludingTasks() {
		Row expectedInput = new Row(5L, "st", new ArrayList<>());
		Row responseFromService = new Row(6L, "ste", Arrays.asList(new Task(1L, 2L, "s", 4, 5)));

		Mockito.when(rowService.updateRow(expectedInput)).thenReturn(responseFromService);

		APIRowPatch row = new APIRowPatch("st");
		APIRow response = api.patchRow(5L, row);

		assertThat(response.getId()).isEqualTo(6L);
		assertThat(response.getTitle()).isEqualTo("ste");
		assertThat(response.getTasks().size()).isEqualTo(1);
		assertThat(response.getTasks().get(0)).isEqualTo(new APITask(1L, 2L, "s", 4, 5));
	}

	@Test
	void patchRowDoesNotAllowNullTitle() {
		try {
			api.patchRow(1L, new APIRowPatch(null));
			fail("Expecting exception");
		} catch (ResponseStatusException e) {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
			assertThat(e.getMessage()).contains("Null is not valid for a row's title.");
		}
	}

	@Test
	void patchThrows404StyleExceptionOnNotFoundRow() {
		Mockito.when(rowService.updateRow(Mockito.any())).thenThrow(new RowService.RowNotFoundException("no!"));

		try {
			api.patchRow(1L, new APIRowPatch("title"));
			fail("Expecting exception");

		} catch (ResponseStatusException e) {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(e.getMessage()).contains("Unable to complete operation.  Either the rowId does not point to an existing row, or you do not have access to it.");

		} catch (Exception e) {
			fail("Expecting ResponseStatusException, instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void deleteRowCallsServiceAndReturnsTheDeletedRow() {
		Mockito.when(rowService.deleteEmptyRowById(55L)).thenReturn(new Row(55L, "Hi i am a title"));

		APIRow deletedRow = api.deleteRow(55L);
		assertThat(deletedRow.getId()).isEqualTo(55L);
		assertThat(deletedRow.getTitle()).isEqualTo("Hi i am a title");
		assertThat(deletedRow.getTasks().size()).isEqualTo(0);
	}

	@Test
	void deleteRowReturns404StyleErrorIfServiceRespondsWithNotFoundException() {
		Mockito.when(rowService.deleteEmptyRowById(55L)).thenThrow(new RowService.RowNotFoundException("NO ROW FOR YOU"));

		try {
			api.deleteRow(55L);
			fail("Expecting exception");

		} catch (ResponseStatusException e) {
			assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
			assertThat(e.getMessage()).contains("Unable to complete operation.  Either the rowId does not point to an existing row, or you do not have access to it.");

		} catch (Exception e) {
			fail("Expecting ResponseStatusException, instead got " + e.getClass().getCanonicalName());
		}
	}

	@Test
	void deleteRowReturnsBadRequestStyleErrorIfServiceFailsForTasksExist() {
		Mockito.when(rowService.deleteEmptyRowById(55L)).thenThrow(new RowService.CannotDeleteNonEmptyRowException("NO ROW FOR YOU"));

		try {
			api.deleteRow(55L);
			fail("Expecting exception");

		} catch (ResponseStatusException e) {
			assertThat(e.getMessage()).contains("Unable to complete operation.  Delete can only be performed on a row that has zero tasks associated with it.  No changes made.");
			assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			fail("Expecting ResponseStatusException, instead got " + e.getClass().getCanonicalName());
		}
	}
}
