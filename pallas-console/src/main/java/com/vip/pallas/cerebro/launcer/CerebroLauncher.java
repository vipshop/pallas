/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.cerebro.launcer;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CerebroLauncher {
	private static Logger logger = LoggerFactory.getLogger(CerebroLauncher.class);

	private  static  final  String CEREBRO_DATA_PATH = "/apps/data/cerebro";
	private  static  final  String CEREBRO_LOGS_PATH = "/apps/logs/cerebro";


	private static List<URL> getUrls(File file) throws MalformedURLException { // NOSONAR
		List<URL> urls = new ArrayList<>();
		if (!file.exists()) {
			return urls;
		}
		if (file.isDirectory()) {
			if ("classes".equals(file.getName())) {
				urls.add(file.toURI().toURL());
				return urls;
			}
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File tmp : files) {
					urls.addAll(getUrls(tmp));
				}
			}
			return urls;
		}
		if (file.isFile()) {
			urls.add(file.toURI().toURL());
		}
		return urls;
	}

	public static void launch(String cerebroHome, String port) {
		String libDri = cerebroHome + File.separator + "lib";
		try {
			List<URL> urls = getUrls(new File(libDri));
			ClassLoader classLoader = new CerebroClassLoader(urls.toArray(new URL[urls.size()]));// NOSONAR
			ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(classLoader);
			System.setProperty("http.port", port);
			System.setProperty("http.address", "127.0.0.1");

			FileUtils.forceMkdir(new File(CEREBRO_DATA_PATH));// NOSONAR
			FileUtils.forceMkdir(new File(CEREBRO_LOGS_PATH));// NOSONAR

			Class<?> mainClass = classLoader.loadClass("play.core.server.ProdServerStart");
			Method mainMethod = mainClass.getMethod("main", String[].class);
			List<String> argList = new ArrayList<>();
			mainMethod.invoke(mainClass, new Object[] { argList.toArray(new String[argList.size()]) });

			Thread.currentThread().setContextClassLoader(currentClassLoader);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}