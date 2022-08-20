package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.request.SimpleMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloWorldAPITest {

	private HelloWorldAPI helloWorldAPI = new HelloWorldAPI();

	@Test
	void testHelloReturnsNiceWelcomeString() {
		SimpleMessage actual = helloWorldAPI.hello();
		SimpleMessage expected = new SimpleMessage("ohai welcome to data from the backend :)");
		assertThat(actual).isEqualTo(expected);
	}
}