package com.poorknight.tpmtoolsbackend.domain.row;

import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectConsistencyValidator;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplateTask;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class RowService {

	private final RowRepository rowRepository;
	private final TaskService taskService;
	private final ProjectConsistencyValidator projectConsistencyValidator;
	private final RowServiceValidator rowServiceValidator;


	public Row saveNewRow(Row newRow) {
		rowServiceValidator.validateRowToSaveThrowingExceptions(newRow);
		return rowRepository.save(newRow);
	}

	public List<Row> getAllRows() {
		Iterable<Row> repositoryResults = rowRepository.findAll();
		List<Row> results = new LinkedList<>();
		for (Row row : repositoryResults) {
			results.add(row);
		}
		return results;
	}

	public Row deleteEmptyRowById(Long rowId) {
		Optional<Row> maybeRow = rowRepository.findById(rowId);
		rowServiceValidator.validateRowDelete(rowId, maybeRow);

		Row row = maybeRow.get();
		rowRepository.deleteById(rowId);
		return row;
	}

	public Row patchRow(RowPatchTemplate rowPatchTemplate) {
		Optional<Row> maybeRow = (rowPatchTemplate.getId() == null) ?
				Optional.empty() :
				rowRepository.findById(rowPatchTemplate.getId());

		rowServiceValidator.validateRowPatch(rowPatchTemplate, maybeRow);
		Row rowToUpdate = maybeRow.get();

		if (rowPatchTemplate.getTitle() != null) {
			rowToUpdate.setTitle(rowPatchTemplate.getTitle());
		}

		if (rowPatchTemplate.getTaskList() != null) {
			projectConsistencyValidator.validateRowChangeSetThrowingExceptions(rowToUpdate, rowPatchTemplate);
			updateTasksInRow(rowPatchTemplate);
		}

		return rowRepository.save(rowToUpdate);
	}

	private void updateTasksInRow(RowPatchTemplate rowPatchTemplate) {
		for (RowPatchTemplateTask tastPatchTemplate : rowPatchTemplate.getTaskList()) {
			Task taskBeingUpdated = taskService.findTaskWithId(tastPatchTemplate.getId());

			if (tastPatchTemplate.getSize() != null) {
				taskBeingUpdated.setSize(tastPatchTemplate.getSize());
			}
			if (tastPatchTemplate.getPosition() != null) {
				taskBeingUpdated.setPosition(tastPatchTemplate.getPosition());
			}

			taskService.updateTask(taskBeingUpdated);
		}
	}
}
