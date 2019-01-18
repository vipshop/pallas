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

package com.vip.pallas.console.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ThrottleUtil {
	private static final String THROTTLE_KEY_ESCLUSTER = "cluster:";
	private static final Map<String, AtomicInteger> throttleWindow = new ConcurrentHashMap<String, AtomicInteger>();

	static AtomicInteger getCounter(String key){
		AtomicInteger counter = throttleWindow.get(key);
		if(counter == null){
			synchronized(throttleWindow){
				counter = throttleWindow.computeIfAbsent(key, k -> new AtomicInteger(0));
			}
		}
		return counter;
	}
	
	public static int esClusterInc(String host) {
		String key = THROTTLE_KEY_ESCLUSTER + host;
		AtomicInteger counter = getCounter(key);
		return counter.incrementAndGet();
	}
	
	public static int esClusterDesc(String host) {
		String key = THROTTLE_KEY_ESCLUSTER + host;
		AtomicInteger counter = getCounter(key);
		return counter.decrementAndGet();
	}
	
}