package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.*;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService;
import com.poorknight.tpmtoolsbackend.domain.projectplan.entity.ProjectPlanPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.row.entity.RowPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.TaskPatchTemplate;
import com.poorknight.tpmtoolsbackend.domain.tasks.entity.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectConsistencyValidator.*;
import static com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPlanControllerTest {

    @InjectMocks
    ProjectPlanController projectPlanController;

    @Mock
    ProjectPlanService projectPlanService;

    @Test
    void canGetAProjectPlanFromTheServiceLayer() {
        Task task1 = new Task(11L, 1L, "task 1 title", 1, 2);
        Task task2 = new Task(12L, 1L, "task 2 title", 2, 3);
        Task task3 = new Task(13L, 2L, "task 3 title", 3, 1);
        Row row1 = new Row(1L, 55L, "row1 title", List.of(task1, task2));
        Row row2 = new Row(2L, 55L, "row2 title", List.of(task3));
        ProjectPlan projectPlan = new ProjectPlan(55L, "a title", List.of(row1, row2));

        when(projectPlanService.getProjectPlan(55L)).thenReturn(projectPlan);

        APIProjectPlan response = projectPlanController.getProjectPlan(55L);

        APITask apiTask1 = new APITask(11L, 1L, "task 1 title", 1, 2);
        APITask apiTask2 = new APITask(12L, 1L, "task 2 title", 2, 3);
        APITask apiTask3 = new APITask(13L, 2L, "task 3 title", 3, 1);
        APIRow apiRow1 = new APIRow(1L, 55L, "row1 title", List.of(apiTask1, apiTask2));
        APIRow apiRow2 = new APIRow(2L, 55L, "row2 title", List.of(apiTask3));
        APIProjectPlan expectedResponse = new APIProjectPlan(55L, "a title", List.of(apiRow1, apiRow2));

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void getProjectPlanThrows404IfServiceThrowsProjectPlanNotFoundException() {
        when(projectPlanService.getProjectPlan(55L)).thenThrow(new ProjectPlanNotFoundException("hi"));

        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> projectPlanController.getProjectPlan(55L));

        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(e.getMessage()).contains("Either the projectPlanId does not point to an existing project plan, or you do not have access to it.");
    }

    @Test
    void patchTranslatesRequestIntoDomainObjectThenCallsServiceAndReturnsItsTranslatedResponse() {

        APIProjectPlanPatchTask task = new APIProjectPlanPatchTask(11L, 1L, 2);
        APIProjectPlanPatchRow rowPatch = new APIProjectPlanPatchRow(1L, List.of(task));
        List<APIProjectPlanPatchRow> rows = List.of(rowPatch);
        APIProjectPlanPatch patchRequest = new APIProjectPlanPatch(55L, rows);

        Task task1 = new Task(11L, 1L, "task 1 title", 1, 2);
        Row row1 = new Row(1L, 55L, "row1 title", List.of(task1));
        Row row2 = new Row(2L, 55L, "row2 title", List.of());
        ProjectPlan projectPlan = new ProjectPlan(55L, "a title", List.of(row1, row2));


        TaskPatchTemplate taskPatchTemplate = TaskPatchTemplate.builder().id(11L).rowId(1L).position(2).build();
        RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, null, List.of(taskPatchTemplate));
        ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(55L, null, List.of(rowPatchTemplate));

        when(projectPlanService.updateProjectPlan(patchTemplate)).thenReturn(projectPlan);

        APIProjectPlan result = projectPlanController.patchProjectPlan(55L, patchRequest);

        APITask apiTask = new APITask(11L, 1L, "task 1 title", 1, 2);
        APIRow apiRow1 = new APIRow(1L, 55L, "row1 title", List.of(apiTask));
        APIRow apiRow2 = new APIRow(2L, 55L, "row2 title", List.of());
        APIProjectPlan expectedResult = new APIProjectPlan(55L, "a title", List.of(apiRow1, apiRow2));
        assertThat(result).isEqualTo(expectedResult);
    }
    @Test
    void patchReturnsTasksSortedByTheirPositionInResponse() {

        APIProjectPlanPatchTask task = new APIProjectPlanPatchTask(11L, 1L, 5);
        APIProjectPlanPatchRow rowPatch = new APIProjectPlanPatchRow(1L, List.of(task));
        List<APIProjectPlanPatchRow> rows = List.of(rowPatch);
        APIProjectPlanPatch patchRequest = new APIProjectPlanPatch(55L, rows);

        Task task1 = new Task(11L, 1L, "task 1", 1, 1);
        Task task2 = new Task(12L, 1L, "task 2", 1, 5);
        Task task3 = new Task(13L, 1L, "task 3", 1, 8);
        Row row1 = new Row(1L, 55L, "row1 title", List.of(task3, task2, task1));
        ProjectPlan projectPlan = new ProjectPlan(55L, "a title", List.of(row1));


        TaskPatchTemplate taskPatchTemplate = TaskPatchTemplate.builder().id(11L).rowId(1L).position(5).build();
        RowPatchTemplate rowPatchTemplate = new RowPatchTemplate(1L, null, List.of(taskPatchTemplate));
        ProjectPlanPatchTemplate patchTemplate = new ProjectPlanPatchTemplate(55L, null, List.of(rowPatchTemplate));

        when(projectPlanService.updateProjectPlan(patchTemplate)).thenReturn(projectPlan);

        APIProjectPlan result = projectPlanController.patchProjectPlan(55L, patchRequest);


        List<APITask> tasksFromResponse = result.getRows().get(0).getTasks();
        assertThat(tasksFromResponse.get(0).getTitle()).isEqualTo("task 1");
        assertThat(tasksFromResponse.get(0).getPosition()).isEqualTo(1);

        assertThat(tasksFromResponse.get(1).getTitle()).isEqualTo("task 2");
        assertThat(tasksFromResponse.get(1).getPosition()).isEqualTo(5);

        assertThat(tasksFromResponse.get(2).getTitle()).isEqualTo("task 3");
        assertThat(tasksFromResponse.get(2).getPosition()).isEqualTo(8);
    }

    @Test
    void patchThrows400ErrorIfServiceThrowsAProjectPlanUpdateConsistencyExceptionPassingSameMessage() {
        when(projectPlanService.updateProjectPlan(any())).thenThrow(new ProjectPlanUpdateConsistencyException("a message"));

        APIProjectPlanPatch patchRequest = new APIProjectPlanPatch(55L, List.of());
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> projectPlanController.patchProjectPlan(55L, patchRequest));

        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(e.getMessage()).contains("a message");
    }

    @Test
    void patchThrows400ExceptionIfATasksRowIdDoesNotMatchTheRowItIsPartOf() {
        APIProjectPlanPatchTask task = new APIProjectPlanPatchTask(11L, 2L, null);
        APIProjectPlanPatchRow row = new APIProjectPlanPatchRow(1L, List.of(task));
        APIProjectPlanPatch patchRequest = new APIProjectPlanPatch(55L, List.of(row));
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> projectPlanController.patchProjectPlan(55L, patchRequest));

        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(e.getMessage()).contains("Tasks much have a rowId that matches the id of the row that contains the task in it's list of tasks.");
    }

    @Test
    void patchThrows400ExceptionIfAProjectPlanIdDoesNotMatchPathParam() {
        APIProjectPlanPatch patchRequest = new APIProjectPlanPatch(55L, List.of());
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
                () -> projectPlanController.patchProjectPlan(77L, patchRequest));

        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(e.getMessage()).contains("The project plan id must match the id in the url.");
    }

}