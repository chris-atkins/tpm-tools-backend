package com.poorknight.tpmtoolsbackend.integrationtests;

import com.fasterxml.jackson.databind.JsonNode;
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


	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		this.deleteAllTasksAndRows();
	}

	@Test
	void canCreateAndGetARow() throws JSONException {
		ResponseEntity<String> postResponse = postNewRow("{\"title\": \"first title\"}");

		assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(postResponse);
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(responseNode);
		assertThat(fieldList.size()).isEqualTo(3);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("title");
		assertThat(fieldList.get(1).getValue().asText()).isEqualTo("first title");
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(2).getKey()).isEqualTo("tasks");
		assertThat(fieldList.get(2).getValue().isArray()).isTrue();
		assertThat(fieldList.get(2).getValue().fields().hasNext()).isFalse();

		Long expectedId = fieldList.get(0).getValue().asLong();

		ResponseEntity<String> getResponse = makeGETRequest("/rows");
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		String expectedResult = String.format("""
				[
					{
						"id": %d,
						"title": "first title",
						"tasks": []
					}
				]
				""", expectedId);
		JSONAssert.assertEquals(expectedResult, getResponse.getBody(), JSONCompareMode.STRICT);
	}


	@Test
	void canSaveARowWithAnEmptyTitle() {
		ResponseEntity<String> response = postNewRow("{\"title\": \"\"}");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(response);
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(responseNode);

		assertThat(fieldList.get(1).getKey()).isEqualTo("title");
		assertThat(fieldList.get(1).getValue().asText()).isEqualTo("");
	}

	@Test
	void canChangeARowsTitleWithAPATCHRequest() throws JSONException {
		ResponseEntity<String> postResponse = postNewRow("{\"title\": \"original title\"}");
		Long rowId = getRowIdFromPostResponse(postResponse);

		ResponseEntity<String> patchResponse = patchRow(rowId,"{\"title\": \"new title\"}");
		assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity<String> getResponse = makeGETRequest("/rows");
		String expectedResult = String.format("""
				[
					{
						"id": %d,
						"title": "new title",
						"tasks": []
					}
				]
				""", rowId);

		JSONAssert.assertEquals(expectedResult, getResponse.getBody(), JSONCompareMode.STRICT);
	}

	private Long getRowIdFromPostResponse(ResponseEntity<String> postResponse) {
		JsonNode responseNode = getRootJsonNode(postResponse);
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(responseNode);
		Long rowId = fieldList.get(0).getValue().asLong();
		return rowId;
	}

	@Test
	void PUTRequestsAreNotAllowed() {
		ResponseEntity<String> postResponse = postNewRow("{\"title\": \"original title\"}");
		Long rowId = getRowIdFromPostResponse(postResponse);

		String putRequestBody  = String.format("""
				{
					"id": %d,
					"title": "new title",
					"tasks": []
				}
				""", rowId);
		ResponseEntity<String> patchResponse = makePUTRequest(putRequestBody, "/rows/" + rowId);
		assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	void addingATaskToARowViaTaskAPIWillResultInItBeingReturnedWithAGet() throws Exception {
		ResponseEntity<String> rowResponse = postNewRow("{\"title\": \"something awesome\"}");
		Long rowId = getRowIdFromPostResponse(rowResponse);

		ResponseEntity<String> taskResponse = postNewTask(rowId, "{\"rowId\": " + rowId + ", \"title\": \"a task!\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(taskResponse);

		ResponseEntity<String> getResponse = makeGETRequest("/rows");
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		String expectedResult = String.format("""
				[
					{
						"id": %d,
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
				""", rowId, taskId, rowId);
		JSONAssert.assertEquals(expectedResult, getResponse.getBody(), JSONCompareMode.STRICT);
	}

	@Test
	void movingATaskAwayFromARowViaTaskAPIWillResultInItNoLongerBeingInTheTaskList() throws Exception {
		ResponseEntity<String> rowResponse1 = postNewRow("{\"title\": \"something awesome\"}");
		ResponseEntity<String> rowResponse2 = postNewRow("{\"title\": \"something that is fine\"}");
		Long rowId1 = getRowIdFromPostResponse(rowResponse1);
		Long rowId2 = getRowIdFromPostResponse(rowResponse2);

		ResponseEntity<String> taskResponse = postNewTask(rowId1, "{\"rowId\": " + rowId1 + ", \"title\": \"a task!\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(taskResponse);

		ResponseEntity<String> getResponse = makeGETRequest("/rows");
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		String expectedResult = String.format("""
				[
					{
						"id": %d,
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
					},
					{
						"id": %d,
						"title": "something that is fine",
						"tasks": []
					}
				]
				""", rowId1, taskId, rowId1, rowId2);
		JSONAssert.assertEquals(expectedResult, getResponse.getBody(), JSONCompareMode.STRICT);

		//Here is the important bit of this test:  especially notice rowId2 -> we're updating the row the task belongs to
		String jsonTaskToUpdate = "{\"id\": " + taskId + ", \"rowId\": " + rowId2 + ", \"title\": \"a task!\", \"size\": 5, \"position\": 3}";
		makePUTRequest(jsonTaskToUpdate, "/rows/" + rowId1 + "/tasks/" + taskId);


		ResponseEntity<String> updatedGetResponse = makeGETRequest("/rows");
		assertThat(updatedGetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		String newExpectedResults = String.format("""
				[
					{
						"id": %d,
						"title": "something awesome",
						"tasks": []
					},
					{
						"id": %d,
						"title": "something that is fine",
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
				""", rowId1, rowId2, taskId, rowId2);
		JSONAssert.assertEquals(newExpectedResults, updatedGetResponse.getBody(), JSONCompareMode.STRICT);
	}

	@Test
	void canDeleteARowAndGetDeletedRowInTheResponse() throws Exception {
		ResponseEntity<String> postResponse = postNewRow("{\"title\": \"original title\"}");
		Long rowId = getRowIdFromPostResponse(postResponse);

		ResponseEntity<String> deleteResponse = makeDELETERequest("/rows/" + rowId);

		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		String expectedResponse = String.format("""
				{
					"id": %d,
					"title": "original title",
					"tasks": []
				}
				""", rowId);
		JSONAssert.assertEquals(expectedResponse, deleteResponse.getBody(), JSONCompareMode.STRICT);

		// row has been deleted - and does not show up in get request
		ResponseEntity<String> getResponse = makeGETRequest("/rows");
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		JSONAssert.assertEquals("[]", getResponse.getBody(), JSONCompareMode.STRICT);
	}

	private ResponseEntity<String> postNewRow(String taskJsonString) {
		ResponseEntity<String> response = makePOSTRequest(taskJsonString, "/rows");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		return response;
	}

	private ResponseEntity<String> patchRow(Long rowId, String taskJsonString) {
		ResponseEntity<String> response = makePATCHRequest(taskJsonString, "/rows/" + rowId);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		return response;
	}

}
