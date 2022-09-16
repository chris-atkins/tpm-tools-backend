package com.poorknight.tpmtoolsbackend.integrationtests;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poorknight.tpmtoolsbackend.TpmToolsBackendApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TpmToolsBackendApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloWorldIT {

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

	private HttpHeaders headers = new HttpHeaders();
	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void testHelloWorldFormat() throws Exception {
		HttpEntity<String> entity = new HttpEntity<>(null, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				createURLWithPort("/hello"),
				HttpMethod.GET, entity, String.class);

		ObjectMapper mapper = new ObjectMapper();
		JsonNode responseNode = mapper.readTree(response.getBody());

		List<Map.Entry<String, JsonNode>> fieldList = new ArrayList<>();
		responseNode.fields().forEachRemaining(fieldList::add);

		assertThat(fieldList.size()).isEqualTo(1);
		assertThat(fieldList.get(0).getKey()).isEqualTo("message");
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();
	}

	private String createURLWithPort(String uri) {
		return "http://localhost:" + port + uri;
	}
}
