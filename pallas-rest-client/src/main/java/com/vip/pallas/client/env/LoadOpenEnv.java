package com.vip.pallas.client.env;

public class LoadOpenEnv extends LoadEnv {

	static {
		consoleQueryUrl = System.getProperty(CONSOLE_QUERY_URL_ENV_KEY, "http://localhost:8080");
		consoleQueryUrl = addSuffixIfNecessary(consoleQueryUrl);
	}

}
