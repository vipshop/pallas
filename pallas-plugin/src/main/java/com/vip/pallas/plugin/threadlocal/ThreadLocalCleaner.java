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

package com.vip.pallas.plugin.threadlocal;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.common.logging.ESLoggerFactory;

/**
 * Created by jamin.li on 14/07/2017.
 */
public class ThreadLocalCleaner {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(ThreadLocalCleaner.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static final ThreadPoolExecutor cleanExecutorService = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS,
			new ArrayBlockingQueue(20480), new ThreadPoolExecutor.DiscardOldestPolicy());

	public static void clean(List<String> classLoaderList) {
		cleanExecutorService.submit(() -> {
			try {
				ConcurrentHashMap<Long, WeakReference<Thread>> threadReferenceMaps = ThreadMarker
						.getThreadReferenceMaps();

				for (Map.Entry<Long, WeakReference<Thread>> entry : threadReferenceMaps.entrySet()) {
					Thread thread = entry.getValue().get();
					if (thread != null) {
						cleanThreadLocal(thread, classLoaderList);
					} else {
						threadReferenceMaps.remove(entry.getKey());
					}
				}
			} catch (Throwable e) {
				LOGGER.error(e.toString(), e);
			}
		});
	}

	private static void cleanThreadLocal(Thread thread, List<String> classLoaderList) throws NoSuchFieldException,
			IllegalAccessException {
		Field threadLocalsField = thread.getClass().getDeclaredField("threadLocals");
		threadLocalsField.setAccessible(true);
		Object threadLocalTable = threadLocalsField.get(thread);

		Field tableField = threadLocalTable.getClass().getDeclaredField("table");
		tableField.setAccessible(true);
		Object table = tableField.get(threadLocalTable);

		Field referentField = Reference.class.getDeclaredField("referent");
		referentField.setAccessible(true);

		List<ThreadLocal> threadLocalList = new ArrayList<>();
		int length = Array.getLength(table);

		for (int i = 0; i < length; i++) {
			Object entry = Array.get(table, i);
			if (entry != null) {
				ThreadLocal threadLocal = (ThreadLocal) referentField.get(entry);
				Object threadLocalVar = ThreadLocalWrapper.get(thread, threadLocal);

				if (threadLocalVar != null && threadLocalVar.getClass() != null
						&& threadLocalVar.getClass().getClassLoader() != null) {

					String classLoaderName = threadLocalVar.getClass().getClassLoader().toString();

					if (classLoaderName != null
							&& classLoaderName.contains("com.vip.pallas.plugin.classloader.PluginClassLoader")
							&& !classLoaderList.contains(classLoaderName)) {
						threadLocalList.add(threadLocal);
					}
				}
			}
		}

		for (ThreadLocal threadLocal : threadLocalList) {
			Object threadLocalVar = ThreadLocalWrapper.get(thread, threadLocal);
			String classLoaderName = threadLocalVar.getClass().getClassLoader().toString();

			ThreadLocalWrapper.remove(thread, threadLocal);

			LOGGER.info("remove threadlocal: {} in classloader: {}", threadLocalVar, classLoaderName);
		}
	}
}