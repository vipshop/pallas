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

package com.vip.pallas.plugin.utils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.logging.ESLoggerFactory;

import com.vip.pallas.plugin.classloader.PluginClassLoader;
import com.vip.pallas.plugin.helper.ClassHelper;
import com.vip.pallas.plugin.helper.FileHelper;
import com.vip.pallas.plugin.search.script.PluginManager;
import com.vip.pallas.plugin.threadlocal.ThreadLocalCleaner;

/**
 * Created by jamin.li on 20/07/2017.
 */
public class PluginLoader {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(PluginLoader.class);

	private static final Map<String /** pluginName **/, String /** classLoaderName **/> PLUGIN_CLASSLOADER_MAP = new HashMap<>();

	private static final String PLUGIN_MANAGER_CLASS = "plugin_manager_class";

	public static void loadPlugin(File file) {
		try {
			Map<String, Class<? extends PluginManager>> preManagerClassLoadedMap = new HashMap<>(
					ClassHelper.MANAGER_CLASS_LOADED_MAP);

			PluginClassLoader pluginClassLoader;

			String filePath = file.getPath();
			String pluginName = parsePluginName(filePath);

			Map<String, String> properties = FileHelper.readProperties(filePath);
			if (properties != null) {
				List<URL> urls = FileHelper.getJars(file);
				pluginClassLoader = new PluginClassLoader(urls.toArray(new URL[urls.size()]),
						ClassHelper.class.getClassLoader());

				Thread currentThread = Thread.currentThread();
				ClassLoader currentContextClassLoader = currentThread.getContextClassLoader();
				currentThread.setContextClassLoader(pluginClassLoader);

				LOGGER.info("begin to load plugin manager class with plugin: {}", pluginName);
				Class<? extends PluginManager> managerClass = ClassHelper.loadManagerClass(pluginClassLoader, pluginName, properties.get(PLUGIN_MANAGER_CLASS));
				initPlugin(pluginName);
				LOGGER.info("loaded plugin: {} with manager class: {}", pluginName, managerClass);

				for (Map.Entry<String, String> entry : properties.entrySet()) {
					String scriptName = entry.getKey().trim();
					if (!PLUGIN_MANAGER_CLASS.equals(scriptName)) {
						ClassHelper.newInstance(
								ClassHelper.loadScriptClass(pluginClassLoader, scriptName, entry.getValue()));
					}
				}

				currentThread.setContextClassLoader(currentContextClassLoader);
				PLUGIN_CLASSLOADER_MAP.put(pluginName, pluginClassLoader.toString());
			}

			// destroy plugin
			if(preManagerClassLoadedMap.containsKey(pluginName)){
				LOGGER.info("begin to destroy plugin: {}", pluginName);
				destroyPlugin(preManagerClassLoadedMap.get(pluginName));
			}

			List<String> classLoaderList = new ArrayList<>(PLUGIN_CLASSLOADER_MAP.values());

			// remove and execute Runtime shutdownHook
			ShutdownHookRemover.remove(classLoaderList);

			// clean threadlocal
			ThreadLocalCleaner.clean(classLoaderList);
		} catch (Throwable e) {
			LOGGER.error(e.toString(), e);
		}
	}

	private static void initPlugin(String pluginName) {
		LOGGER.info("begin to initialize plugin: {}", pluginName);
		ClassHelper.newManagerInstance(pluginName).init();
		LOGGER.info("initialized plugin: {}", pluginName);
	}

	private static void destroyPlugin(Class<? extends PluginManager> managerClass)
			throws IllegalAccessException, InstantiationException {
		LOGGER.info("begin to destroy plugin with class: {}", managerClass);
		managerClass.newInstance().destroy();
		LOGGER.info("destroyed plugin with class: {}", managerClass);
	}

	private static String parsePluginName(String filePath){
		String nameAndVer = filePath.substring(filePath.lastIndexOf('/') + 1);
		return nameAndVer.substring(0, nameAndVer.lastIndexOf('-'));
	}
}