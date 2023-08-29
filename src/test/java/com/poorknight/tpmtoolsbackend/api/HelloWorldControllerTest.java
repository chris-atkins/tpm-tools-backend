package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.APIMessage;
import com.poorknight.tpmtoolsbackend.domain.hello.HelloMessage;
import com.poorknight.tpmtoolsbackend.domain.hello.HelloService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HelloWorldControllerTest {

	@InjectMocks
    HelloWorldController helloWorldController;

	@Mock
	HelloService helloService;

	@Test
	void testHelloReturnsNiceWelcomeString() {
		Mockito.when(helloService.getRandomHelloMessage()).thenReturn(new HelloMessage("ohai from DB"));

		APIMessage actual = helloWorldController.hello();
		APIMessage expected = new APIMessage("ohai from DB");
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void throwsGenericExceptionIfExceptionFromService() {
		Mockito.when(helloService.getRandomHelloMessage()).thenThrow(new RuntimeException("oh balls user should not see this message."));

		try {
			helloWorldController.hello();
			Assertions.fail("Expecting an exception");
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("There was a problem retrieving the data.");
			assertThat(e).isInstanceOf(RuntimeException.class);
		}
	}
}