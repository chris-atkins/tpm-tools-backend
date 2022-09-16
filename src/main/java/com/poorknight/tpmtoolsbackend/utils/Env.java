package com.poorknight.tpmtoolsbackend.utils;

import org.springframework.stereotype.Component;

@Component
public class Env {

	private Env(){
		// private == no instances please thx
	}

	public static String getEnvironmentVariable(String name) {
		return System.getenv(name);
	}

}
