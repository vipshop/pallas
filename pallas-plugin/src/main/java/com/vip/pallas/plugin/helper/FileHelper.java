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

package com.vip.pallas.plugin.helper;

import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jamin.li on 15/06/2017.
 */
public class FileHelper {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(FileHelper.class);

	public static List<URL> getJars(File file) throws MalformedURLException {
		List<URL> urls = new ArrayList<>();
		if (!file.exists()) {
			return urls;
		}
		if (file.isFile()) {
			if (file.getName().endsWith(".jar")) {
				urls.add(file.toURI().toURL());
			}
		} else if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File f : files) {
					urls.addAll(getJars(f));
				}
			}
			return urls;
		}
		return urls;
	}

	public static List<File> listSubDir(String root) {
		File[] fs = new File(root).listFiles();
		if (fs != null && fs.length > 0) {
			List<File> list = new ArrayList<>(fs.length);
			for (File f : fs) {
				if (f.isDirectory()) {
					list.add(f.getAbsoluteFile());
				}
			}
			return list;
		}
		return null;
	}

	public static List<String> listConfigPlugins(String pluginPath) throws IOException {
		FileInputStream inputStream = null;

		try {
			List<String> list = new ArrayList<>();
			Properties prop = new Properties();

			inputStream = new FileInputStream(new File(pluginPath + "/pallas-plugin.properties"));

			prop.load(inputStream);
			Pattern pattern = Pattern.compile("plugin\\.(.*)\\.class");
			for (Map.Entry entry : prop.entrySet()) {
				Matcher m = pattern.matcher(entry.getKey().toString());
				if (m.find()) {
					list.add(m.group(1));
				}
			}

			return list;
		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
		} finally {
			if(inputStream != null){
				inputStream.close();
			}
		}

		return null;
	}

	public static Map<String, String> readProperties(String pluginPath) throws IOException {
        FileInputStream inputStream = null;

		try {
			Map<String, String> map = new HashMap<>();
			Properties prop = new Properties();

            inputStream = new FileInputStream(new File(pluginPath + "/pallas-plugin.properties"));

            prop.load(inputStream);
			Pattern pattern = Pattern.compile("plugin\\.(.*)\\.class");
			for (Map.Entry entry : prop.entrySet()) {
				Matcher m = pattern.matcher(entry.getKey().toString());
				if (m.find()) {
					map.put(m.group(1), entry.getValue().toString());
				}
			}

			map.put("plugin_manager_class", prop.getProperty("plugin_manager_class"));

			return map;
		} catch (FileNotFoundException e){
			LOGGER.error("{}, the path would be ignored.", e.toString());
		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
		} finally {
		    if(inputStream != null){
                inputStream.close();
            }
        }
        return null;
	}
}