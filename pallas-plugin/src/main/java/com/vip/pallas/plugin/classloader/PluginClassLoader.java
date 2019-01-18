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

package com.vip.pallas.plugin.classloader;

import org.elasticsearch.common.logging.ESLoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by jamin.li on 27/06/2017.
 */
public class PluginClassLoader extends URLClassLoader {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(PluginClassLoader.class);

	private ClassLoader esClassLoader;

	public PluginClassLoader(URL[] urls, ClassLoader esClassLoader) {
		super(urls);
		this.esClassLoader = esClassLoader;
	}

	@Override
	public Class<?> loadClass(String className) {
		synchronized (getClassLoadingLock(className)) {
			Class<?> findClass = findLoadedClass(className);

			if (findClass == null) {
				try {
					findClass = super.loadClass(className);
				} catch (Exception e) { // NOSONAR
				}
			}

			if (findClass == null) {
				try {
					findClass = esClassLoader.loadClass(className);
				} catch (Exception e) { // NOSONAR
					LOGGER.error("fail to load class: {} cause by: {}", className, e.toString());
				}
			}

			return findClass;
		}
	}

	@Override
	public URL getResource(String name) {
		URL url = findResource(name);

		if (url == null) {
			url = super.getResource(name);
		}

		if (url == null) {
			url = esClassLoader.getResource(name);
		}

		return url;
	}
}