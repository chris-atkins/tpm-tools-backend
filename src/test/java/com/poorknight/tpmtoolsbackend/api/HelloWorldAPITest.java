package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.response.SimpleMessage;
import com.poorknight.tpmtoolsbackend.hello.HelloMessage;
import com.poorknight.tpmtoolsbackend.hello.HelloService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HelloWorldAPITest {

	@InjectMocks
	HelloWorldAPI helloWorldAPI;

	@Mock
	HelloService helloService;

	@Test
	void testHelloReturnsNiceWelcomeString() {
		Mockito.when(helloService.getRandomHelloMessage()).thenReturn(new HelloMessage("ohai from DB"));

		SimpleMessage actual = helloWorldAPI.hello();
		SimpleMessage expected = new SimpleMessage("hi.. ohai from DB");
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void throwsGenericExceptionIfExceptionFromService() {
		Mockito.when(helloService.getRandomHelloMessage()).thenThrow(new RuntimeException("oh balls user should not see this message."));

		try {
			helloWorldAPI.hello();
			Assertions.fail("Expecting an exception");
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("There was a problem retrieving the data.");
			assertThat(e).isInstanceOf(RuntimeException.class);
		}
	}
}