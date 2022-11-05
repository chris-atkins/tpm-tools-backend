package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.response.SimpleMessage;
import com.poorknight.tpmtoolsbackend.hello.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

@RestController
public class HelloWorldAPI {

	@Autowired
	private HelloService helloService;

	@GetMapping("/hello")
	public SimpleMessage hello() {
		try {
			return new SimpleMessage("hi.. " + helloService.getRandomHelloMessage().getMessage());
		} catch (Exception e) {
			throw new RuntimeException("There was a problem retrieving the data.");
		}
	}
}
