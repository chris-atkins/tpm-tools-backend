package com.poorknight.tpmtoolsbackend.integrationtests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poorknight.tpmtoolsbackend.TpmToolsBackendApplication;
import com.poorknight.tpmtoolsbackend.domain.BaseTestWithDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TpmToolsBackendApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseIntegrationTestWithDatabase extends BaseTestWithDatabase {

	@DynamicPropertySource
	static void registerDBProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", db::getJdbcUrl);
		registry.add("spring.datasource.username", db::getUsername);
		registry.add("spring.datasource.password", db::getPassword);
	}

	@LocalServerPort
	private int port;

	protected HttpHeaders headers;
	protected TestRestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		restTemplate = new TestRestTemplate();
	}

	protected String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}


	protected JsonNode getRootJsonNode(ResponseEntity<String> response) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readTree(response.getBody());
	}


	protected List<Map.Entry<String, JsonNode>> getAllFieldsForNode(JsonNode jsonNode) {
		List<Map.Entry<String, JsonNode>> fieldList = new ArrayList<>();
		jsonNode.fields().forEachRemaining(fieldList::add);
		return fieldList;
	}


	protected ResponseEntity<String> makeGETRequest(String path) {
		return buildRequestForRestMethod(null, path, HttpMethod.GET);
	}


	protected ResponseEntity<String> makeDELETERequest(String path) {
		return buildRequestForRestMethod(null, path, HttpMethod.DELETE);
	}


	protected ResponseEntity<String> makePOSTRequest(String jsonRequestBodyString, String path) {
		return buildRequestForRestMethod(jsonRequestBodyString, path, HttpMethod.POST);
	}


	protected ResponseEntity<String> makePUTRequest(String jsonRequestBodyString, String path) {
		return buildRequestForRestMethod(jsonRequestBodyString, path, HttpMethod.PUT);
	}


	private ResponseEntity<String> buildRequestForRestMethod(String jsonRequestBodyString, String path, HttpMethod restMethod) {
		HttpEntity<String> entity = new HttpEntity<>(jsonRequestBodyString, headers);
		return restTemplate.exchange(createURLWithPort(path), restMethod, entity, String.class);
	}
}
