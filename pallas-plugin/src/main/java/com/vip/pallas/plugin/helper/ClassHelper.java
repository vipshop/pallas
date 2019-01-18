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

import com.vip.pallas.plugin.search.script.PallasExecutableScript;
import com.vip.pallas.plugin.search.script.PallasSearchScript;
import com.vip.pallas.plugin.search.script.PluginManager;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jamin.li on 19/06/2017.
 */
public class ClassHelper {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(ClassHelper.class);

	public static final Map<String /** scriptName **/, Class<? extends PallasExecutableScript>> SCRIPT_CLASS_LOADED_MAP = new ConcurrentHashMap<>();

	public static final Map<String /** pluginName **/, Class<? extends PluginManager>> MANAGER_CLASS_LOADED_MAP = new ConcurrentHashMap<>();

	public static Class<? extends PallasExecutableScript> loadScriptClass(URLClassLoader pluginClassLoader, String scriptName, String scriptClass) {
		try {
			Class<? extends PallasExecutableScript> scriptClazz = (Class<? extends PallasExecutableScript>) pluginClassLoader
					.loadClass(scriptClass);
			if(scriptName != null && scriptClass != null){
				LOGGER.info("loaded plugin: {} with script class: {}", scriptName, scriptClass);
				SCRIPT_CLASS_LOADED_MAP.put(scriptName, scriptClazz);
			}
			return scriptClazz;
		} catch (Throwable e) {
			LOGGER.error(e.toString(), e);
			return null;
		}
	}

	public static Class<? extends PluginManager> loadManagerClass(URLClassLoader pluginClassLoader, String pluginName,
			String managerClass) {
		try {
			Class<? extends PluginManager> managerClazz = (Class<? extends PluginManager>) pluginClassLoader
					.loadClass(managerClass);
			if(pluginName != null && managerClazz != null){
				LOGGER.info("loaded plugin: {} with manager class: {}", pluginName, managerClass);
				MANAGER_CLASS_LOADED_MAP.put(pluginName, managerClazz);
			}
			return managerClazz;
		} catch (Throwable e) {
			LOGGER.error(e.toString(), e);
			return null;
		}
	}

	public static PallasExecutableScript newInstance(Class<? extends PallasExecutableScript> scriptClass) {
		try {
			return scriptClass.getConstructor(PallasSearchScript.class, Map.class).newInstance(null,
					new HashMap<String, Object>());
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
			return null;
		}
	}

	public static PallasExecutableScript newInstance(String scriptName, PallasSearchScript pallasSearchScript,
			Map<String, Object> params) {
		if (!SCRIPT_CLASS_LOADED_MAP.containsKey(scriptName)) {
			throw new RuntimeException("couldn't found plugin by name: " + scriptName);
		}
		try {
			return SCRIPT_CLASS_LOADED_MAP.get(scriptName).getConstructor(PallasSearchScript.class, Map.class)
					.newInstance(pallasSearchScript, params);
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
			return null;
		}
	}

	public static PluginManager newManagerInstance(String pluginName) {
		if (!MANAGER_CLASS_LOADED_MAP.containsKey(pluginName)) {
			throw new RuntimeException("couldn't found plugin manager by name: " + pluginName);
		}
		try {
			return MANAGER_CLASS_LOADED_MAP.get(pluginName).newInstance();
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
			return null;
		}
	}
}