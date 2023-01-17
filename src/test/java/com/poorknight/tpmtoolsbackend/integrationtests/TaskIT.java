package com.poorknight.tpmtoolsbackend.integrationtests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poorknight.tpmtoolsbackend.TpmToolsBackendApplication;
import com.poorknight.tpmtoolsbackend.domain.tasks.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TpmToolsBackendApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TaskIT {

	@Container
	public static MySQLContainer db = (MySQLContainer) new MySQLContainer(DockerImageName.parse("mysql:8.0.27"))
			.withDatabaseName("tpm-tools")
			.withUsername("Chris")
			.withPassword("theBestPassword");

	@DynamicPropertySource
	static void registerDBProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", db::getJdbcUrl);
		registry.add("spring.datasource.username", db::getUsername);
		registry.add("spring.datasource.password", db::getPassword);
	}

	@LocalServerPort
	private int port;

	@SpyBean
	private TaskService taskService;

	private HttpHeaders headers = new HttpHeaders();
	private TestRestTemplate restTemplate = new TestRestTemplate();

	@BeforeEach
	void setUp() {
		headers.setContentType(MediaType.APPLICATION_JSON);

		try {
			deleteAllTasks();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void deleteAllTasks() throws Exception {
		Properties connectionProps = new Properties();
		connectionProps.setProperty("user", db.getUsername());
		connectionProps.setProperty("password", db.getPassword());
		Connection connection = DriverManager.getConnection(db.getJdbcUrl(), connectionProps);
		Statement statement = connection.createStatement();

		statement.executeUpdate("DELETE FROM TASK");

		statement.close();
		connection.close();
	}

	@Test
	public void testResponseHasIdAndTitleOnSuccessfulPOST() throws Exception {
		String body = "{\"title\": \"the best title\"}";
		HttpEntity<String> entity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/task"),
				HttpMethod.POST, entity, String.class);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseNode = mapper.readTree(response.getBody());

		List<Map.Entry<String, JsonNode>> fieldList = new ArrayList<>();
		responseNode.fields().forEachRemaining(fieldList::add);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

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
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/task"),
				HttpMethod.POST, entity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("do not provide an id")).isGreaterThan(0);
	}

	@Test
	public void postTaskDoesNotAcceptExtraFields() throws Exception {
		String body = "{\"someExtraField\": \"hi\", \"title\": \"the best title\"}";
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/task"),
				HttpMethod.POST, entity, String.class);

		System.out.println(response.getBody());
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().indexOf("Unrecognized field \\\"someExtraField\\\"")).isGreaterThan(0);
	}

	@Test
	void getAllTasksReturnsAListOfTasksThatHaveBeenSaved() throws Exception {
		postNewTask("{\"title\": \"the best title\"}");
		postNewTask("{\"title\": \"the second best title\"}");

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/task"),
				HttpMethod.GET, null, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseNode = mapper.readTree(response.getBody());

		assertThat(responseNode.isArray()).isTrue();

		List<JsonNode> taskResponseList = new ArrayList<>();
		responseNode.elements().forEachRemaining(taskResponseList::add);

		assertThat(taskResponseList.size()).isEqualTo(2);

		assertThatNodeIsWellFormedTaskWithIdAndTitleOf(taskResponseList.get(0), "the best title");
		assertThatNodeIsWellFormedTaskWithIdAndTitleOf(taskResponseList.get(0), "the best title");
	}

	private void assertThatNodeIsWellFormedTaskWithIdAndTitleOf(JsonNode nodeToCheck, String title) {
		List<Map.Entry<String, JsonNode>> fieldList = new ArrayList<>();
		nodeToCheck.fields().forEachRemaining(fieldList::add);

		assertThat(fieldList.size()).isEqualTo(2);

		assertThat(fieldList.get(0).getKey()).isEqualTo("id");
		assertThat(fieldList.get(0).getValue().asLong()).isGreaterThan(0);
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();

		assertThat(fieldList.get(1).getKey()).isEqualTo("title");
		assertThat(fieldList.get(1).getValue().asText()).isEqualTo(title);
		assertThat(fieldList.get(1).getValue().fields().hasNext()).isFalse();
	}

	private void postNewTask(String taskJsonString) {
		String body = taskJsonString;
		HttpEntity<String> entity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/task"),
				HttpMethod.POST, entity, String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
