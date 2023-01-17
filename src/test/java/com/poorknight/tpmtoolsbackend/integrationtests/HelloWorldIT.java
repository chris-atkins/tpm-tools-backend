package com.poorknight.tpmtoolsbackend.integrationtests;


import com.fasterxml.jackson.databind.JsonNode;
import com.poorknight.tpmtoolsbackend.domain.hello.HelloService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloWorldIT extends BaseIntegrationTestWithDatabase {

	@SpyBean
	private HelloService helloService;

	@Test
	public void testHelloWorldFormat() throws Exception {
		ResponseEntity<String> response = makeGETRequest("/hello");

		JsonNode responseNode = getRootJsonNode(response);
		List<Map.Entry<String, JsonNode>> fieldList = getAllFieldsForNode(responseNode);

		assertThat(fieldList.size()).isEqualTo(1);
		assertThat(fieldList.get(0).getKey()).isEqualTo("message");
		assertThat(fieldList.get(0).getValue().fields().hasNext()).isFalse();
	}


	@Test
	void helloEndpointReturns500ErrorWhenFailureRetrievingData() {
		Mockito.when(helloService.getRandomHelloMessage()).thenThrow(new RuntimeException());
		ResponseEntity<String> response = makeGETRequest("/hello");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
