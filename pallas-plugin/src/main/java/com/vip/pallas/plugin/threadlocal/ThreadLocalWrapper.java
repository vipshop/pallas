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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by jamin.li on 18/07/2017.
 */
public class ThreadLocalWrapper {

	@SuppressWarnings("rawtypes")
	public static Object get(Thread thread, ThreadLocal threadLocal) {
		try {
			Method getMapMethod = threadLocal.getClass().getDeclaredMethod("getMap", Thread.class);
			getMapMethod.setAccessible(true);
			Object map = getMapMethod.invoke(threadLocal, thread);
			if (map != null) {
				Method getEntryMethod = map.getClass().getDeclaredMethod("getEntry", ThreadLocal.class);
				getEntryMethod.setAccessible(true);
				Object entry = getEntryMethod.invoke(map, threadLocal);
				if (entry != null) {
					Field valueField = entry.getClass().getDeclaredField("value");
					valueField.setAccessible(true);
					return valueField.get(entry);
				}
			}
			return setInitialValue(thread, threadLocal);
		} catch (Exception e) { // NOSONAR
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public static void remove(Thread thread, ThreadLocal threadLocal) {
		try {
			Method getMapMethod = threadLocal.getClass().getDeclaredMethod("getMap", Thread.class);
			getMapMethod.setAccessible(true);
			Object map = getMapMethod.invoke(threadLocal, thread);
			if (map != null) {
				Method removeMethod = map.getClass().getDeclaredMethod("remove", ThreadLocal.class);
				removeMethod.setAccessible(true);
				removeMethod.invoke(map, threadLocal);
			}
		} catch (Exception e) { // NOSONAR
		}
	}

	@SuppressWarnings("rawtypes")
	private static Object setInitialValue(Thread thread, ThreadLocal threadLocal) {
		try {
			Method initialValueMethod = threadLocal.getClass().getDeclaredMethod("initialValue");
			initialValueMethod.setAccessible(true);
			Object value = initialValueMethod.invoke(threadLocal);

			Method getMapMethod = threadLocal.getClass().getDeclaredMethod("getMap", Thread.class);
			getMapMethod.setAccessible(true);
			Object map = getMapMethod.invoke(threadLocal, thread);

			if (map != null) {
				Method setMethod = map.getClass().getDeclaredMethod("set", ThreadLocal.class, Object.class);
				setMethod.setAccessible(true);
				setMethod.invoke(map, threadLocal, value);
			} else {
				Method createMapMethod = threadLocal.getClass().getDeclaredMethod("createMap", Thread.class,
						Object.class);
				createMapMethod.setAccessible(true);
				createMapMethod.invoke(threadLocal, thread, value);
			}
			return value;
		} catch (Exception e) { // NOSONAR
		}
		return null;
	}
}