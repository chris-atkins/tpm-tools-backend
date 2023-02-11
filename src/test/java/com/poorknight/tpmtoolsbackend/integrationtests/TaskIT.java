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

	@Override
	@BeforeEach
	void setUp() {
		super.setUp();
		this.deleteAllTasks();
	}


	@Test
	public void testResponseHasIdAndTitleAndSizeOnSuccessfulPOST() throws Exception {
		String body = "{\"title\": \"the best title\", \"size\": 5}";
		ResponseEntity<String> response = makePOSTRequest(body, "/task");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(response);
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(responseNode);
		assertThat(fieldList.size()).isEqualTo(3);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("title");
		assertThat(fieldList.get(1).getValue().asText()).isEqualTo("the best title");
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(2).getKey()).isEqualTo("size");
		assertThat(fieldList.get(2).getValue().asInt()).isEqualTo(5);
		assertThat(fieldList.get(2).getValue().fields().hasNext()).isFalse();
	}


	@Test
	public void errorsIfAnIdIsProvidedWhenPOSTingTask() throws Exception {
		String body = "{\"id\": 55, \"title\": \"the best title\", \"size\": 5}";

		ResponseEntity<String> response = makePOSTRequest(body, "/task");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("do not provide an id")).isGreaterThan(0);
	}


	@Test
	public void postTaskDoesNotAcceptExtraFields() throws Exception {
		String body = "{\"someExtraField\": \"hi\", \"title\": \"the best title\", \"size\": 5}";

		ResponseEntity<String> response =makePOSTRequest(body, "/task");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("Unrecognized field \\\"someExtraField\\\"")).isGreaterThan(0);
	}

	@Test
	void putTaskCanChangeTitleAndSizeOnAnExistingTask() throws Exception {
		postNewTask("{\"title\": \"the best title\", \"size\": 5}");

		ResponseEntity<String> getResponse = makeGETRequest("/task");
		List<JsonNode> taskList = buildTaskListFromGetResponse(getResponse);
		assertThat(taskList.size()).isEqualTo(1);
		assertThat(taskList.get(0).get("title").asText()).isEqualTo("the best title");
		assertThat(taskList.get(0).get("size").asInt()).isEqualTo(5);

		Long taskId = taskList.get(0).get("id").asLong();

		String jsonTaskToUpdate = "{\"id\": " + taskId + ", \"title\": \"a fine title\", \"size\": 8}";
		ResponseEntity<String> putResponse = makePUTRequest(jsonTaskToUpdate, "/task/" + taskId);

		String expectedResult = String.format("""
				{
					"id": %d,
					"title": "a fine title",
					"size": 8 
				}
				""", taskId);
		JSONAssert.assertEquals(expectedResult, putResponse.getBody(), JSONCompareMode.STRICT);

		getResponse = makeGETRequest("/task");
		taskList = buildTaskListFromGetResponse(getResponse);
		assertThat(taskList.size()).isEqualTo(1);
		assertThat(taskList.get(0).get("title").asText()).isEqualTo("a fine title");
		assertThat(taskList.get(0).get("size").asInt()).isEqualTo(8);
	}

	@Test
	public void putTaskDoesNotAcceptExtraFields() throws Exception {
		ResponseEntity<String> postResponse = postNewTask("{\"title\": \"first title\", \"size\": 5}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = String.format("""
				{"id": %d, "someExtraField": "hi", "title": "the best title", "size": 8}
				""", taskId);

		ResponseEntity<String> response = makePUTRequest(body, "/task/" + taskId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("Unrecognized field \\\"someExtraField\\\"")).isGreaterThan(0);
	}

	private Long getTaskIdFromPostResponse(ResponseEntity<String> postResponse) throws Exception {
		JsonNode responseNode = getRootJsonNode(postResponse);
		return responseNode.get("id").asLong();
	}

	@Test
	public void putTaskDoesNotAcceptBodyWithNoId() throws Exception {
		ResponseEntity<String> postResponse = postNewTask("{\"title\": \"first title\", \"size\": 5}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = """
				{"title": "the best title", "size": 8}
				""";

		ResponseEntity<String> response = makePUTRequest(body, "/task/" + taskId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("provide an id")).isGreaterThan(0);
	}

	@Test
	public void putTaskDoesNotAcceptBodyWithMismatchedIdComparedToUrl() throws Exception {
		ResponseEntity<String> postResponse = postNewTask("{\"title\": \"first title\", \"size\": 5}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = String.format("""
				{"id": %d, "title": "the best title", "size": 8}
				""", (taskId + 1));

		ResponseEntity<String> response = makePUTRequest(body, "/task/" + taskId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("id must match")).isGreaterThan(0);
	}

	@Test
	public void putTaskAcceptsBodyWithNoChanges() throws Exception {
		ResponseEntity<String> postResponse = postNewTask("{\"title\": \"first title\", \"size\": 5}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		String body = String.format("""
				{"id": %d, "title": "first title", "size": 5}
				""", taskId);

		ResponseEntity<String> response = makePUTRequest(body, "/task/" + taskId);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}


	@Test
	void deleteTaskRemovesATask() throws Exception {
		ResponseEntity<String> postResponse = postNewTask("{\"title\": \"a title\", \"size\": 5}");
		Long taskId = getTaskIdFromPostResponse(postResponse);

		assertThat(findNumberOfTasksReturnedFromGetAll()).isEqualTo(1);

		ResponseEntity<String> response = makeDELETERequest("/task/" + taskId);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(findNumberOfTasksReturnedFromGetAll()).isEqualTo(0);
	}


	@Test
	void deleteTask404sIfNoTaskExistsWithPassedId() throws Exception {
		ResponseEntity<String> response = makeDELETERequest("/task/74");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}


	private int findNumberOfTasksReturnedFromGetAll() throws Exception {
		ResponseEntity<String> response = makeGETRequest("/task");
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
	void getAllTasksReturnsAListOfTasksThatHaveBeenSaved() throws Exception {
		postNewTask("{\"title\": \"the best title\", \"size\": 5}");
		postNewTask("{\"title\": \"the second best title\", \"size\": 6}");

		ResponseEntity<String> response = makeGETRequest("/task");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(response);
		assertThat(responseNode.isArray()).isTrue();

		List<JsonNode> taskResponseList = new ArrayList<>();
		responseNode.elements().forEachRemaining(taskResponseList::add);

		assertThat(taskResponseList.size()).isEqualTo(2);

		assertThatNodeIsWellFormedTaskWithIdAndTitleOf(taskResponseList.get(0), "the best title", 5);
		assertThatNodeIsWellFormedTaskWithIdAndTitleOf(taskResponseList.get(1), "the second best title", 6);
	}


	private void assertThatNodeIsWellFormedTaskWithIdAndTitleOf(JsonNode nodeToCheck, String title, int size) {
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(nodeToCheck);

		assertThat(fieldList.size()).isEqualTo(3);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("title");
		assertThat(fieldList.get(1).getValue().asText()).isEqualTo(title);
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(2).getKey()).isEqualTo("size");
		assertThat(fieldList.get(2).getValue().asInt()).isEqualTo(size);
		assertThat(fieldList.get(2).getValue().fields().hasNext()).isFalse();
	}


	private ResponseEntity<String> postNewTask(String taskJsonString) {
		ResponseEntity<String> response = makePOSTRequest(taskJsonString, "/task");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		return response;
	}

}
