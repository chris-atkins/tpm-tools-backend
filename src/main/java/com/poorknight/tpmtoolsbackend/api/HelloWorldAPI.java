package com.poorknight.tpmtoolsbackend.api;

import com.poorknight.tpmtoolsbackend.api.entity.request.SimpleMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldAPI {

	@GetMapping("/hello")
	public SimpleMessage hello() {
		return new SimpleMessage("ohai welcome to data from the backend :)");
	}
}
