package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.APIProjectPlan;
import com.poorknight.tpmtoolsbackend.api.entity.APIRow;
import com.poorknight.tpmtoolsbackend.api.entity.APITask;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlan;
import com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService;
import com.poorknight.tpmtoolsbackend.domain.row.entity.Row;
import com.poorknight.tpmtoolsbackend.domain.tasks.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.poorknight.tpmtoolsbackend.domain.projectplan.ProjectPlanService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}