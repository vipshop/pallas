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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.common.logging.ESLoggerFactory;

/**
 * Created by jamin.li on 19/07/2017.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ShutdownHookRemover {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(ShutdownHookRemover.class);

	private static final ThreadPoolExecutor removeExecutorService = new ThreadPoolExecutor(1, 5, 20, TimeUnit.SECONDS,
			new ArrayBlockingQueue(200), new ThreadPoolExecutor.DiscardOldestPolicy());

	public static void remove(List<String> classLoaderList)
			throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		Class clazz = Class.forName("java.lang.ApplicationShutdownHooks");
		Field field = clazz.getDeclaredField("hooks");
		field.setAccessible(true);
		IdentityHashMap<Thread, Thread> hooks = (IdentityHashMap<Thread, Thread>) field.get(null);

		if (hooks != null) {
			Set<Thread> threadSets = new HashSet<>();

			for (Thread thread : hooks.keySet()) {
				ClassLoader classLoader = thread.getClass().getClassLoader();
				if (classLoader != null) {
					String classLoaderName = classLoader.toString();
					if (classLoaderName != null
							&& classLoaderName.contains("com.vip.pallas.plugin.classloader.PluginClassLoader")
							&& !classLoaderList.contains(classLoaderName)) {
						threadSets.add(thread);
					}
				}
			}

			for (Thread thread : threadSets) {
				Runtime.getRuntime().removeShutdownHook(thread);
				removeExecutorService.submit(thread);
				LOGGER.info("remove shutdownHook: " + thread + " successed");
			}
		}
	}
}