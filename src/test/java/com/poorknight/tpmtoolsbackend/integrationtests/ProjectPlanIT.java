package com.poorknight.tpmtoolsbackend.integrationtests;

import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;

public class ProjectPlanIT extends BaseIntegrationTestWithDatabase {


    private Long projectPlanId;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        deleteAllTasksAndRowsAndProjectPlans();
        projectPlanId = this.createProjectPlanWithSQLOnly("Some kind of title");

    }

    @Test
    void canGetAProjectPlanById() throws JSONException {

        Long row1Id = this.createRowWithSQLOnly(projectPlanId, "row1 title");
        Long row2Id = this.createRowWithSQLOnly(projectPlanId, "row2 title");
        Long taskId = this.createTaskWithSQLOnly(row1Id, "task title");
        ResponseEntity<String> response = this.makeGETRequest("/api/v1/project-plans/" + projectPlanId);

        String expectedResponse = String.format("""
                {
                    "id": %d,
                    "title": "Some kind of title",
                    "rows": [
                        {
                            "id": %d,
                            "projectPlanId": %d,
                            "title": "row1 title",
                            "tasks": [
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task title",
                                    "position": 1,
                                    "size": 1
                                }
                            ]
                        },
                        {
                            "id": %d,
                            "projectPlanId": %d,
                            "title": "row2 title",
                            "tasks": []
                        }
                    ]
                }
            """, projectPlanId, row1Id, projectPlanId, taskId, row1Id, row2Id, projectPlanId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(expectedResponse, response.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    void getProjectPlanReturns404IfNoProjectPlanWithIdExists() {
        ResponseEntity<String> response = this.makeGETRequest("/api/v1/project-plans/555");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
