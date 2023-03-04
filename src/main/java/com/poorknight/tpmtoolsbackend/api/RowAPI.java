package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.response.APIRow;
import com.poorknight.tpmtoolsbackend.api.entity.response.APIRowPatch;
import com.poorknight.tpmtoolsbackend.domain.row.Row;
import com.poorknight.tpmtoolsbackend.domain.row.RowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RowAPI {

	@Autowired
	private RowService rowService;

	@GetMapping(value = "/rows")
	public List<APIRow> getAllRows() {
		return rowService.getAllRows().stream()
				.map(APIRow::fromDomainObject)
				.collect(Collectors.toList());
	}

	@PostMapping(value = "/rows", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APIRow postNewRow(@RequestBody APIRow row) {
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

	@PatchMapping(value = "/rows/{rowId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public APIRow patchRow(@PathVariable Long rowId, @RequestBody APIRowPatch rowPatch) {
		validatePatchInputThrowingException(rowPatch);
		Row row = new Row(rowId, rowPatch.getTitle());
		return APIRow.fromDomainObject(rowService.updateRow(row));
	}

	private void validatePatchInputThrowingException(APIRowPatch rowPatch) {
		if (rowPatch.getTitle() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Null is not valid for a row's title.");
		}
	}
}
