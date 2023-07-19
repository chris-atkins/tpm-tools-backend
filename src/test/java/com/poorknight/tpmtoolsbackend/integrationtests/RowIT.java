package com.poorknight.tpmtoolsbackend.integrationtests;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RowIT extends BaseIntegrationTestWithDatabase {

	private Long projectPlanId;

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		this.deleteAllTasksAndRowsAndProjectPlans();
		projectPlanId = this.createProjectPlanWithSQLOnly("tpm-tools");
	}

	@Test
	void canCreateAndGetARow() throws JSONException {
		ResponseEntity<String> postResponse = postNewRow(projectPlanId, String.format("""
			{
  				"projectPlanId": %d,
  				"title": "first title"
  			}""", projectPlanId));

		assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(postResponse);
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(responseNode);
		assertThat(fieldList.size()).isEqualTo(4);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("projectPlanId");
		assertThat(fieldList.get(1).getValue().asLong()).isEqualTo(projectPlanId);
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(2).getKey()).isEqualTo("title");
		assertThat(fieldList.get(2).getValue().asText()).isEqualTo("first title");
		assertThat(fieldList.get(2).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(3).getKey()).isEqualTo("tasks");
		assertThat(fieldList.get(3).getValue().isArray()).isTrue();
		assertThat(fieldList.get(3).getValue().fields().hasNext()).isFalse();

		Long expectedId = fieldList.get(0).getValue().asLong();

		ResponseEntity<String> getResponse = getAllRows(projectPlanId);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		String expectedResult = String.format("""
				[
					{
						"id": %d,
						"projectPlanId": %d,
						"title": "first title",
						"tasks": []
					}
				]
				""", expectedId, projectPlanId);
		JSONAssert.assertEquals(expectedResult, getResponse.getBody(), JSONCompareMode.STRICT);
	}

	@Test
	void canSaveARowWithAnEmptyTitle() {
		ResponseEntity<String> response = postNewRow(projectPlanId, String.format("""
			{
  				"projectPlanId": %d,
  				"title": ""
  			}""", projectPlanId));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(response);
		assertThat(responseNode.get("title").asText()).isEqualTo("");
	}

	@Test
	void canChangeARowsTitleWithAPATCHRequest() throws JSONException {
		ResponseEntity<String> postResponse = postNewRow(projectPlanId, String.format("""
			{
  				"projectPlanId": %d,
  				"title": "original title"
  			}""", projectPlanId));
		Long rowId = getRowIdFromPostResponse(postResponse);

		ResponseEntity<String> patchResponse = patchRow(rowId, projectPlanId, "{\"title\": \"new title\"}");
		assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity<String> getResponse = getAllRows(projectPlanId);
		String expectedResult = String.format("""
				[
					{
						"id": %d,
						"projectPlanId": %d,
						"title": "new title",
						"tasks": []
					}
				]
				""", rowId, projectPlanId);

		JSONAssert.assertEquals(expectedResult, getResponse.getBody(), JSONCompareMode.STRICT);
	}

	@Test
	@SneakyThrows
	void canChangeSizeAndPositionOfRowsTasksWithAPATCHRequest() throws JSONException {
		ResponseEntity<String> postResponse = postNewRow(projectPlanId, String.format("""
			{
  				"projectPlanId": %d,
  				"title": "original title"
  			}""", projectPlanId));
		Long rowId = getRowIdFromPostResponse(postResponse);

		ResponseEntity<String> task1Response = postNewTask(rowId, String.format("""
				{
					"rowId": %d,
					"title": "task 1",
					"size": 1,
					"position": 1
				}
				""", rowId));
		Long task1Id = getTaskIdFromPostResponse(task1Response);

		ResponseEntity<String> task2Response = postNewTask(rowId, String.format("""
				{
					"rowId": %d,
					"title": "task 2",
					"size": 2,
					"position": 2
				}
				""", rowId));
		Long task2Id = getTaskIdFromPostResponse(task2Response);

		ResponseEntity<String> patchResponse = patchRow(rowId,projectPlanId, String.format("""
    		{
				"tasks": [
					{
						"id": %d,
						"size": 2,
						"position": 2
					},
					{
						"id": %d,
						"size": 3,
						"position": 3
					}
				]
    		}
    	""", task1Id, task2Id));
		assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity<String> getResponse = getAllRows(projectPlanId);
		String expectedResult = String.format("""
				[
					{
						"id": %d,
						"projectPlanId": %d,
						"title": "original title",
						"tasks": [
							{
								"id": %d,
								"rowId": %d,
								"title": "task 1",
								"size": 2,
								"position": 2
							},
							{
								"id": %d,
								"rowId": %d,
								"title": "task 2",
								"size": 3,
								"position": 3
							}
						]
					}
				]
				""", rowId, projectPlanId, task1Id, rowId, task2Id, rowId);

		JSONAssert.assertEquals(expectedResult, getResponse.getBody(), JSONCompareMode.STRICT);
	}

	@Test
	void PUTRequestsAreNotAllowed() {
		ResponseEntity<String> postResponse = postNewRow(projectPlanId, String.format("""
			{
  				"projectPlanId": %d,
  				"title": "original title"
  			}""", projectPlanId));
		Long rowId = getRowIdFromPostResponse(postResponse);

		String putRequestBody  = String.format("""
				{
					"id": %d,
					"projectPlanId": %d,
					"title": "new title",
					"tasks": []
				}
				""", rowId, projectPlanId);
		ResponseEntity<String> patchResponse = makePUTRequest(putRequestBody, String.format("/api/v1/project-plans/%d/rows/%d", projectPlanId, rowId));
		assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	void addingATaskToARowViaTaskAPIWillResultInItBeingReturnedWithAGet() throws Exception {
		ResponseEntity<String> rowResponse = postNewRow(projectPlanId, String.format("""
			{
  				"projectPlanId": %d,
  				"title": "something awesome"
  			}""", projectPlanId));
		Long rowId = getRowIdFromPostResponse(rowResponse);

		ResponseEntity<String> taskResponse = postNewTask(rowId, "{\"rowId\": " + rowId + ", \"title\": \"a task!\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(taskResponse);

		ResponseEntity<String> getResponse = getAllRows(projectPlanId);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		String expectedResult = String.format("""
				[
					{
						"id": %d,
						"projectPlanId": %d,
						"title": "something awesome",
						"tasks": [
							{
								"id": %d,
								"rowId": %d,
								"title": "a task!",
								"size": 5,
								"position": 3
							}
						]
					}
				]
				""", rowId, projectPlanId, taskId, rowId);
		JSONAssert.assertEquals(expectedResult, getResponse.getBody(), JSONCompareMode.STRICT);
	}

	@Test
	void canDeleteARowAndGetDeletedRowInTheResponse() throws Exception {
		ResponseEntity<String> postResponse = postNewRow(projectPlanId, String.format("""
			{
  				"projectPlanId": %d,
  				"title": "original title"
  			}""", projectPlanId));
		Long rowId = getRowIdFromPostResponse(postResponse);

		ResponseEntity<String> deleteResponse = deleteRow(rowId, projectPlanId);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		String expectedResponse = String.format("""
				{
					"id": %d,
					"projectPlanId": %d,
					"title": "original title",
					"tasks": []
				}
				""", rowId, projectPlanId);
		JSONAssert.assertEquals(expectedResponse, deleteResponse.getBody(), JSONCompareMode.STRICT);

		// row has been deleted - and does not show up in get request
		ResponseEntity<String> getResponse = getAllRows(projectPlanId);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		JSONAssert.assertEquals("[]", getResponse.getBody(), JSONCompareMode.STRICT);
	}

	private ResponseEntity<String> postNewRow(Long projectPlanId, String taskJsonString) {
		String path = buildPostUrlPath(projectPlanId);
		ResponseEntity<String> response = makePOSTRequest(taskJsonString, path);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		return response;
	}

	private ResponseEntity<String> getAllRows(Long projectPlanId) {
		String path = buildGetUrlPath(projectPlanId);
		return makeGETRequest(path);
	}

	private ResponseEntity<String> patchRow(Long rowId, Long projectPlanId, String rowPatchJsonString) {
		String path = buildPatchUrlPath(rowId, projectPlanId);
		ResponseEntity<String> response = makePATCHRequest(rowPatchJsonString, path);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		return response;
	}

	private ResponseEntity<String> deleteRow(Long rowId, Long projectPlanId) {
		String path = buildDeleteUrlPath(rowId, projectPlanId);
		return makeDELETERequest(path);
	}

	private static String buildGetUrlPath(Long projectPlanId) {
		return String.format("/api/v1/project-plans/%d/rows", projectPlanId);
	}

	private static String buildPostUrlPath(Long projectPlanId) {
		return buildGetUrlPath(projectPlanId);
	}

	private static String buildPatchUrlPath(Long rowId, Long projectPlanId) {
		return String.format("/api/v1/project-plans/%d/rows/%d", projectPlanId, rowId);
	}

	private static String buildDeleteUrlPath(Long rowId, Long projectPlanId) {
		return buildPatchUrlPath(rowId, projectPlanId);
	}

	private Long getRowIdFromPostResponse(ResponseEntity<String> postResponse) {
		JsonNode responseNode = getRootJsonNode(postResponse);
		return responseNode.get("id").asLong();
	}
}
