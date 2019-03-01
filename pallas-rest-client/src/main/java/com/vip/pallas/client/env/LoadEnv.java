package com.vip.pallas.client.env;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoadEnv {

	private static final Logger log = LoggerFactory.getLogger(LoadEnv.class);

	public static String consoleQueryUrl;

	private static LoadEnv envInstance;

	public static final String CONSOLE_QUERY_URL_ENV_KEY = "VIP_PALLAS_CONSOLE_QUERY_URL";

	public static final String QUERY_URL_SUFFIX = "/pallas/ss/query_pslist_and_domain.json";

	static {
		initEnvImpl();
	}

	public static String addSuffixIfNecessary(String consoleQueryUrl) {
		if (!consoleQueryUrl.endsWith(".json")) {
			consoleQueryUrl += consoleQueryUrl.endsWith("/") ? StringUtils.substringAfter(QUERY_URL_SUFFIX, "/") : QUERY_URL_SUFFIX;
		}
		log.warn("console url located to: {}", consoleQueryUrl);
		return consoleQueryUrl;
	}

	public static LoadEnv envInstance() {
		return envInstance;
	}

	private static void initEnvImpl() {
		try {
			// discover LoadEnv
			ServiceLoader<LoadEnv> loader = ServiceLoader.load(LoadEnv.class);
			Iterator<LoadEnv> iter = loader.iterator();
			while (iter.hasNext()) {
				envInstance = iter.next();
				log.info("LoadEnv implementation found: {}", envInstance.getClass().getName());
				return;
			}
			// log error, fallback
			envInstance = new LoadOpenEnv();
			log.error("LoadEnv implementation not found, fallback to: {}", envInstance.getClass().getName());
		} catch (Throwable e) {
			// log error, fallback
			envInstance = new LoadOpenEnv();
			log.error("LoadEnv implementation not found, fallback to: {}", envInstance.getClass().getName());
		}
	}

}
