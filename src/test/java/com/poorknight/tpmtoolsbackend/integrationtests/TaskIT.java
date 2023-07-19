package com.poorknight.tpmtoolsbackend.integrationtests;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskIT extends BaseIntegrationTestWithDatabase {

	private Long rowId1;
	private Long rowId2;

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		this.deleteAllTasksAndRowsAndProjectPlans();
		Long projectPlanId = createProjectPlanWithSQLOnly("THE project plan");
		rowId1 = this.createRowWithSQLOnly(projectPlanId, "Row 1");
		rowId2 = this.createRowWithSQLOnly(projectPlanId, "Row 2");
	}

	@Test
	public void testTaskResponseHasIdAndRowIdAndTitleAndSizeAndPositionOnSuccessfulPOST() throws Exception {
		String body = "{\"rowId\": " + rowId1 + ", \"title\": \"the best title\", \"size\": 5, \"position\": 3}";
		ResponseEntity<String> response = makePOSTRequest(body, "/rows/" + rowId1 + "/tasks");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(response);
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(responseNode);
		assertThat(fieldList.size()).isEqualTo(5);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("rowId");
		assertThat(fieldList.get(1).getValue().asLong()).isEqualTo(rowId1);
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(2).getKey()).isEqualTo("title");
		assertThat(fieldList.get(2).getValue().asText()).isEqualTo("the best title");
		assertThat(fieldList.get(2).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(3).getKey()).isEqualTo("size");
		assertThat(fieldList.get(3).getValue().asInt()).isEqualTo(5);
		assertThat(fieldList.get(3).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(4).getKey()).isEqualTo("position");
		assertThat(fieldList.get(4).getValue().asInt()).isEqualTo(3);
		assertThat(fieldList.get(4).getValue().fields().hasNext()).isFalse();
	}

	@Test
	public void errorsIfAnIdIsProvidedWhenPOSTingTask() throws Exception {
		String body = "{\"id\": 55, \"rowId\": " + rowId1 + ", \"title\": \"the best title\", \"size\": 5, \"position\": 3}";

		ResponseEntity<String> response = makePOSTRequest(body, "/rows/" + rowId1 + "/tasks");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("do not provide an id")).isGreaterThan(0);
	}

	@Test
	public void postTaskDoesNotAcceptExtraFields() throws Exception {
		String body = "{\"someExtraField\": \"hi\", \"rowId\": " + rowId1 + ", \"title\": \"the best title\", \"size\": 5, \"position\": 3}";

		ResponseEntity<String> response =makePOSTRequest(body, "/rows/" + rowId1 + "/tasks");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("Unrecognized field \\\"someExtraField\\\"")).isGreaterThan(0);
	}

	@Test
	void patchTaskCanChangeTitleOnAnExistingTask() throws Exception {
		postNewTask(rowId1, "{\"rowId\": " + rowId1 + ", \"title\": \"the best title\", \"size\": 5, \"position\": 3}");

		ResponseEntity<String> getResponse = makeGETRequest("/rows/" + rowId1 + "/tasks");
		List<JsonNode> taskList = buildTaskListFromGetResponse(getResponse);
		assertThat(taskList.size()).isEqualTo(1);
		assertThat(taskList.get(0).get("title").asText()).isEqualTo("the best title");
		assertThat(taskList.get(0).get("size").asInt()).isEqualTo(5);
		assertThat(taskList.get(0).get("position").asInt()).isEqualTo(3);

		Long taskId = taskList.get(0).get("id").asLong();

		String jsonTaskToUpdate = "{\"id\": " + taskId + ", \"title\": \"a fine title\"}";
		ResponseEntity<String> patchResponse = makePATCHRequest(jsonTaskToUpdate, "/rows/" + rowId1 + "/tasks/" + taskId);

		String expectedResult = String.format("""
				{
					"id": %d,
					"rowId": %d,
					"title": "a fine title",
					"size": 5,
					"position": 3
				}
				""", taskId, rowId1);
		assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		JSONAssert.assertEquals(expectedResult, patchResponse.getBody(), JSONCompareMode.STRICT);

		getResponse = makeGETRequest("/rows/" + rowId1 + "/tasks");
		taskList = buildTaskListFromGetResponse(getResponse);
		assertThat(taskList.size()).isEqualTo(1);
		assertThat(taskList.get(0).get("rowId").asLong()).isEqualTo(rowId1);
		assertThat(taskList.get(0).get("title").asText()).isEqualTo("a fine title");
		assertThat(taskList.get(0).get("size").asInt()).isEqualTo(5);
		assertThat(taskList.get(0).get("position").asInt()).isEqualTo(3);
	}

	@Test
	public void patchTaskDoesNotAcceptExtraFields() throws Exception {
		ResponseEntity<String> postResponse = postNewTask(rowId1, "{\"rowId\": " + rowId1 + ", \"title\": \"first title\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = String.format("""
				{"id": %d, "someExtraField": "hi", "title": "the best title"}
				""", taskId, rowId1);

		ResponseEntity<String> response = makePATCHRequest(body, "/rows/" + rowId1 + "/tasks/" + taskId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("Unrecognized field \\\"someExtraField\\\"")).isGreaterThan(0);
	}

	@Test
	public void patchTaskDoesNotAcceptBodyWithNoId() throws Exception {
		ResponseEntity<String> postResponse = postNewTask(rowId1, "{\"rowId\": " + rowId1 + ", \"title\": \"first title\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = String.format("""
				{"title": "the best title"}
				""", rowId1);

		ResponseEntity<String> response = makePATCHRequest(body, "/rows/" + rowId1 + "/tasks/" + taskId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("provide an id")).isGreaterThan(0);
	}

	@Test
	public void patchTaskDoesNotAcceptBodyWithMismatchedIdComparedToUrl() throws Exception {
		ResponseEntity<String> postResponse = postNewTask(rowId1, "{\"rowId\": " + rowId1 + ", \"title\": \"first title\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = String.format("""
				{"id": %d, "title": "the best title"}
				""", (taskId + 1), rowId1);

		ResponseEntity<String> response = makePATCHRequest(body, "/rows/" + rowId1 + "/tasks/" + taskId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("id must match")).isGreaterThan(0);
	}

	@Test
	public void patchTaskAcceptsTitleWithNoChanges() throws Exception {
		ResponseEntity<String> postResponse = postNewTask(rowId1, "{\"rowId\": " + rowId1 + ", \"title\": \"first title\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = String.format("""
				{"id": %d, "title": "first title"}
				""", taskId, rowId1);

		ResponseEntity<String> response = makePATCHRequest(body, "/rows/" + rowId1 + "/tasks/" + taskId);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	void putIsNotAllowedAndReturns405() throws Exception {
		ResponseEntity<String> postResponse = postNewTask(rowId1, "{\"rowId\": " + rowId1 + ", \"title\": \"first title\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = String.format("""
				{"id": %d, "rowId": %d, "title": "first title", "size": 5, "position": 3}
				""", taskId, rowId1);

		ResponseEntity<String> response = makePUTRequest(body, "/rows/" + rowId1 + "/tasks/" + taskId);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
	}

	@Test
	void deleteTaskRemovesATask() throws Exception {
		ResponseEntity<String> postResponse = postNewTask(rowId1, "{\"rowId\": " + rowId1 + ", \"title\": \"a title\", \"size\": 5, \"position\": 3}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		assertThat(findNumberOfTasksReturnedFromGetAll()).isEqualTo(1);

		ResponseEntity<String> response = makeDELETERequest("/rows/" + rowId1 + "/tasks/" + taskId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(findNumberOfTasksReturnedFromGetAll()).isEqualTo(0);
	}

	@Test
	void deleteTask404sIfNoTaskExistsWithPassedId() throws Exception {
		ResponseEntity<String> response = makeDELETERequest("/rows/" + rowId1 + "/tasks/74");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	private int findNumberOfTasksReturnedFromGetAll() throws Exception {
		ResponseEntity<String> response = makeGETRequest("/rows/" + rowId1 + "/tasks");
		List<JsonNode> tasks = buildTaskListFromGetResponse(response);
		return tasks.size();
	}

	private List<JsonNode> buildTaskListFromGetResponse(ResponseEntity<String> response) throws Exception {
		JsonNode responseNode = getRootJsonNode(response);
		List<JsonNode> taskResponseList = new ArrayList<>();
		responseNode.elements().forEachRemaining(taskResponseList::add);
		return taskResponseList;
	}

	@Test
	void getAllTasksReturnsAListOfTasksThatHaveBeenSavedToARow() throws Exception {
		postNewTask(rowId1,"{\"rowId\": " + rowId1 + ", \"title\": \"the best title\", \"size\": 5, \"position\": 3}");
		postNewTask(rowId1,"{\"rowId\": " + rowId1 + ", \"title\": \"the second best title\", \"size\": 6, \"position\": 4}");
		postNewTask(rowId2,"{\"rowId\": " + rowId2 + ", \"title\": \"the worst title\", \"size\": 7, \"position\": 5}");

		ResponseEntity<String> response = makeGETRequest("/rows/" + rowId1 + "/tasks");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(response);
		assertThat(responseNode.isArray()).isTrue();

		List<JsonNode> taskResponseList = new ArrayList<>();
		responseNode.elements().forEachRemaining(taskResponseList::add);

		assertThat(taskResponseList.size()).isEqualTo(2);

		assertThatNodeIsWellFormedTaskWithFields(taskResponseList.get(0), rowId1,"the best title", 5, 3);
		assertThatNodeIsWellFormedTaskWithFields(taskResponseList.get(1), rowId1,"the second best title", 6, 4);
	}

	private void assertThatNodeIsWellFormedTaskWithFields(JsonNode nodeToCheck, Long rowId, String title, int size, int position) {
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(nodeToCheck);

		assertThat(fieldList.size()).isEqualTo(5);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("rowId");
		assertThat(fieldList.get(1).getValue().asLong()).isEqualTo(rowId);
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(2).getKey()).isEqualTo("title");
		assertThat(fieldList.get(2).getValue().asText()).isEqualTo(title);
		assertThat(fieldList.get(2).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(3).getKey()).isEqualTo("size");
		assertThat(fieldList.get(3).getValue().asInt()).isEqualTo(size);
		assertThat(fieldList.get(3).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(4).getKey()).isEqualTo("position");
		assertThat(fieldList.get(4).getValue().asInt()).isEqualTo(position);
		assertThat(fieldList.get(4).getValue().fields().hasNext()).isFalse();
	}
}
