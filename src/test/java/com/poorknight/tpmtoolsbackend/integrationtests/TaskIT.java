package com.poorknight.tpmtoolsbackend.integrationtests;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	public void testResponseHasIdAndTitleOnSuccessfulPOST() throws Exception {
		String body = "{\"title\": \"the best title\"}";
		ResponseEntity<String> response = makePOSTRequest(body, "/task");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(response);
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(responseNode);
		assertThat(fieldList.size()).isEqualTo(2);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("title");
		assertThat(fieldList.get(1).getValue().asText()).isEqualTo("the best title");
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();
	}


	@Test
	public void errorsWithIdWhenPOSTingTask() throws Exception {
		String body = "{\"id\": 55, \"title\": \"the best title\"}";

		ResponseEntity<String> response = makePOSTRequest(body, "/task");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("do not provide an id")).isGreaterThan(0);
	}


	@Test
	public void postTaskDoesNotAcceptExtraFields() throws Exception {
		String body = "{\"someExtraField\": \"hi\", \"title\": \"the best title\"}";

		ResponseEntity<String> response =makePOSTRequest(body, "/task");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("Unrecognized field \\\"someExtraField\\\"")).isGreaterThan(0);
	}


	@Test
	void getAllTasksReturnsAListOfTasksThatHaveBeenSaved() throws Exception {
		postNewTask("{\"title\": \"the best title\"}");
		postNewTask("{\"title\": \"the second best title\"}");

		ResponseEntity<String> response = makeGETRequest("/task");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		JsonNode responseNode = getRootJsonNode(response);
		assertThat(responseNode.isArray()).isTrue();

		List<JsonNode> taskResponseList = new ArrayList<>();
		responseNode.elements().forEachRemaining(taskResponseList::add);

		assertThat(taskResponseList.size()).isEqualTo(2);

		assertThatNodeIsWellFormedTaskWithIdAndTitleOf(taskResponseList.get(0), "the best title");
		assertThatNodeIsWellFormedTaskWithIdAndTitleOf(taskResponseList.get(0), "the best title");
	}


	private void assertThatNodeIsWellFormedTaskWithIdAndTitleOf(JsonNode nodeToCheck, String title) {
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(nodeToCheck);

		assertThat(fieldList.size()).isEqualTo(2);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("title");
		assertThat(fieldList.get(1).getValue().asText()).isEqualTo(title);
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();
	}


	private void postNewTask(String taskJsonString) {
		ResponseEntity<String> response = makePOSTRequest(taskJsonString, "/task");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
