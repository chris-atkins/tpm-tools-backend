package com.poorknight.tpmtoolsbackend.integrationtests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poorknight.tpmtoolsbackend.TpmToolsBackendApplication;
import com.poorknight.tpmtoolsbackend.domain.BaseTestWithDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
	protected RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		restTemplate = new RestTemplate(requestFactory);
	}

	protected String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}

	protected JsonNode getRootJsonNode(ResponseEntity<String> response) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readTree(response.getBody());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	protected ResponseEntity<String> makePATCHRequest(String jsonRequestBodyString, String path) {
		return buildRequestForRestMethod(jsonRequestBodyString, path, HttpMethod.PATCH);
	}

	protected ResponseEntity<String> makePUTRequest(String jsonRequestBodyString, String path) {
		return buildRequestForRestMethod(jsonRequestBodyString, path, HttpMethod.PUT);
	}

	private ResponseEntity<String> buildRequestForRestMethod(String jsonRequestBodyString, String path, HttpMethod restMethod) {
		HttpEntity<String> entity = new HttpEntity<>(jsonRequestBodyString, headers);
		if (jsonRequestBodyString == null || jsonRequestBodyString.equals("")) {
			entity = new HttpEntity<>(jsonRequestBodyString);  // don't require content-type headers if no body
		}
		try {
			return this.restTemplate.exchange(createURLWithPort(path), restMethod, entity, String.class);

		} catch (HttpClientErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		} catch (HttpServerErrorException e) {
			return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	ResponseEntity<String> postNewTask(Long rowId, String taskJsonString) {
		ResponseEntity<String> response = makePOSTRequest(taskJsonString, "/rows/" + rowId + "/tasks");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		return response;
	}

	Long getTaskIdFromPostResponse(ResponseEntity<String> postResponse) throws Exception {
		JsonNode responseNode = getRootJsonNode(postResponse);
		return responseNode.get("id").asLong();
	}
}
