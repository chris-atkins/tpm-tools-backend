package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.APIRow;
import com.poorknight.tpmtoolsbackend.api.entity.APIRowPatch;
import com.poorknight.tpmtoolsbackend.domain.row.RowServiceValidator;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.RowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/project-plans/{projectPlanId}/rows")
public class RowAPI {

	@Autowired
	private RowService rowService;

	@GetMapping
	public List<APIRow> getAllRows() {
		return rowService.getAllRows().stream()
				.map(APIRow::fromDomainObject)
				.collect(Collectors.toList());
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APIRow postNewRow(@PathVariable Long projectPlanId, @RequestBody APIRow row) {
		validatePostedRowThrowingExceptions(row);

		return APIRow.fromDomainObject(rowService.saveNewRow(row.toDomainObject()));
	}

	private void validatePostedRowThrowingExceptions(APIRow row) {
		if (row.getId() != null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Do not include an id when saving a new row.");
		}

		if (row.getTasks() != null && row.getTasks().size() > 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Do not include tasks when saving a new Row.  Save the row with an empty task list, and then update each task with the row's id.");
		}
	}

	@PatchMapping(value = "/{rowId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APIRow patchRow(@PathVariable Long rowId, @RequestBody APIRowPatch rowPatch) {
		RowPatchTemplate row = rowPatch.toDomainObject(rowId);
		try {
			Row updatedRow = rowService.patchRow(row);
			return APIRow.fromDomainObject(updatedRow);

		} catch(RowServiceValidator.RowNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to complete operation.  Either the rowId does not point to an existing row, or you do not have access to it.");
		}
	}

	@DeleteMapping(value="/{rowId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public APIRow deleteRow(@PathVariable Long rowId) {
		try {
			return APIRow.fromDomainObject(rowService.deleteEmptyRowById(rowId));

		} catch (RowServiceValidator.RowNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to complete operation.  Either the rowId does not point to an existing row, or you do not have access to it.");
		} catch (RowServiceValidator.CannotDeleteNonEmptyRowException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to complete operation.  Delete can only be performed on a row that has zero tasks associated with it.  No changes made.");
		}
	}
}
