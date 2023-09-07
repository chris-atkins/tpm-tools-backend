package com.poorknight.tpmtoolsbackend.integrationtests;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void patchProjectPlanCanMoveTasksWithinARow() throws JSONException {

        Long row1Id = this.createRowWithSQLOnly(projectPlanId, "row1 title");
        Long row2Id = this.createRowWithSQLOnly(projectPlanId, "row2 title");
        Long task1Id = this.createTaskWithSQLOnly(row1Id, "task1 title", 1, 1);
        Long task2Id = this.createTaskWithSQLOnly(row1Id, "task2 title", 1, 2);
        Long task3Id = this.createTaskWithSQLOnly(row1Id, "task3 title", 2, 3);
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
                                    "title": "task1 title",
                                    "position": 1,
                                    "size": 1
                                },
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task2 title",
                                    "position": 2,
                                    "size": 1
                                },
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task3 title",
                                    "position": 3,
                                    "size": 2
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
            """, projectPlanId, row1Id, projectPlanId, task1Id, row1Id, task2Id, row1Id, task3Id, row1Id, row2Id, projectPlanId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(expectedResponse, response.getBody(), JSONCompareMode.STRICT);


        String patchBody = String.format("""
                    {
                        "id": %d,
                        "rows": [
                            {
                                "id": %d,
                                "tasks": [
                                    {
                                        "id": %d,
                                        "rowId": %d,
                                        "position": 1
                                    },
                                    {
                                        "id": %d,
                                        "rowId": %d,
                                        "position": 4
                                    },
                                    {
                                        "id": %d,
                                        "rowId": %d,
                                        "position": 6
                                    }
                                ]
                            }
                        ]
                    }
                """, projectPlanId, row1Id, task3Id, row1Id, task1Id, row1Id, task2Id, row1Id);
        ResponseEntity<String> patchResponse = this.makePATCHRequest(patchBody, "/api/v1/project-plans/" + projectPlanId);

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);


        String expectedNewProjectPlan = String.format("""
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
                                    "title": "task3 title",
                                    "position": 1,
                                    "size": 2
                                },
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task1 title",
                                    "position": 4,
                                    "size": 1
                                },
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task2 title",
                                    "position": 6,
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
            """, projectPlanId, row1Id, projectPlanId, task3Id, row1Id, task1Id, row1Id, task2Id, row1Id, row2Id, projectPlanId);

        ResponseEntity<String> getResponse = this.makeGETRequest("/api/v1/project-plans/" + projectPlanId);

        // both the patch result and the new get should have the same full and changed project plan
        JSONAssert.assertEquals(expectedNewProjectPlan, getResponse.getBody(), JSONCompareMode.STRICT);
        JSONAssert.assertEquals(expectedNewProjectPlan, patchResponse.getBody(), JSONCompareMode.STRICT);
    }

    @Test
    void patchProjectPlanThrows400IfPatchResultsInOverlappingTasks() throws JSONException {
        Long row1Id = this.createRowWithSQLOnly(projectPlanId, "row1 title");
        Long row2Id = this.createRowWithSQLOnly(projectPlanId, "row2 title");
        Long task1Id = this.createTaskWithSQLOnly(row1Id, "task1 title", 1, 1);
        Long task2Id = this.createTaskWithSQLOnly(row1Id, "task2 title", 1, 2);
        Long task3Id = this.createTaskWithSQLOnly(row1Id, "task3 title", 2, 3);
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
                                    "title": "task1 title",
                                    "position": 1,
                                    "size": 1
                                },
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task2 title",
                                    "position": 2,
                                    "size": 1
                                },
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task3 title",
                                    "position": 3,
                                    "size": 2
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
            """, projectPlanId, row1Id, projectPlanId, task1Id, row1Id, task2Id, row1Id, task3Id, row1Id, row2Id, projectPlanId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(expectedResponse, response.getBody(), JSONCompareMode.STRICT);


        String patchBody = String.format("""
                    {
                        "id": %d,
                        "rows": [
                            {
                                "id": %d,
                                "tasks": [
                                    {
                                        "id": %d,
                                        "rowId": %d,
                                        "position": 1
                                    }
                                ]
                            }
                        ]
                    }
                """, projectPlanId, row1Id, task3Id, row1Id);
        ResponseEntity<String> patchResponse = this.makePATCHRequest(patchBody, "/api/v1/project-plans/" + projectPlanId);

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(patchResponse.getBody()).contains("The proposed change results in more than one task occupying the same space");
    }

    @Test
    void patchProjectPlanCanMoveTasksBetweenRows() throws JSONException {
        Long row1Id = this.createRowWithSQLOnly(projectPlanId, "row1 title");
        Long row2Id = this.createRowWithSQLOnly(projectPlanId, "row2 title");
        Long task1Id = this.createTaskWithSQLOnly(row1Id, "task1 title", 1, 1);
        Long task2Id = this.createTaskWithSQLOnly(row1Id, "task2 title", 1, 2);
        Long task3Id = this.createTaskWithSQLOnly(row2Id, "task3 title", 1, 1);
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
                                    "title": "task1 title",
                                    "position": 1,
                                    "size": 1
                                },
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task2 title",
                                    "position": 2,
                                    "size": 1
                                }
                            ]
                        },
                        {
                            "id": %d,
                            "projectPlanId": %d,
                            "title": "row2 title",
                            "tasks": [
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task3 title",
                                    "position": 1,
                                    "size": 1
                                }
                            ]
                        }
                    ]
                }
            """, projectPlanId, row1Id, projectPlanId, task1Id, row1Id, task2Id, row1Id, row2Id, projectPlanId, task3Id, row2Id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(expectedResponse, response.getBody(), JSONCompareMode.STRICT);


        String patchBody = String.format("""
                    {
                        "id": %d,
                        "rows": [
                            {
                                "id": %d,
                                "tasks": [
                                    {
                                        "id": %d,
                                        "rowId": %d
                                    },
                                    {
                                        "id": %d,
                                        "rowId": %d,
                                        "position": 2
                                    }
                                ]
                            }
                        ]
                    }
                """, projectPlanId, row2Id, task1Id, row2Id, task3Id, row2Id);
        ResponseEntity<String> patchResponse = this.makePATCHRequest(patchBody, "/api/v1/project-plans/" + projectPlanId);

        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);


        String expectedNewProjectPlan = String.format("""
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
                                    "title": "task2 title",
                                    "position": 2,
                                    "size": 1
                                }
                            ]
                        },
                        {
                            "id": %d,
                            "projectPlanId": %d,
                            "title": "row2 title",
                            "tasks": [
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task1 title",
                                    "position": 1,
                                    "size": 1
                                },
                                {
                                    "id": %d,
                                    "rowId": %d,
                                    "title": "task3 title",
                                    "position": 2,
                                    "size": 1
                                }
                            ]
                        }
                    ]
                }
            """, projectPlanId, row1Id, projectPlanId, task2Id, row1Id, row2Id, projectPlanId, task1Id, row2Id, task3Id, row2Id);

        ResponseEntity<String> getResponse = this.makeGETRequest("/api/v1/project-plans/" + projectPlanId);

        System.out.println(patchResponse.getBody());
        System.out.println(getResponse.getBody());
        System.out.println(expectedNewProjectPlan.replace("\n", "").replace("  ", "").replace(": ", ":"));
        System.out.println("patch, then get, then expected");
        // both the patch result and the new get should have the same full and changed project plan
        JSONAssert.assertEquals(expectedNewProjectPlan, getResponse.getBody(), JSONCompareMode.STRICT);
        JSONAssert.assertEquals(expectedNewProjectPlan, patchResponse.getBody(), JSONCompareMode.STRICT);
    }

}
