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

package com.vip.pallas.search.timeout;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vip.pallas.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.monitor.ServerStatus;
import com.vip.pallas.thread.PallasThreadFactory;

public class TimeoutRetryController {

	private static Object lock = new Object();
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutRetryController.class);
	private static ExecutorService timeoutRetryGovernorExecutor = Executors
			.newSingleThreadExecutor(new PallasThreadFactory("timeout-retry-governor"));
	private static ConcurrentLinkedQueue<AsyncCall> callQueue = new ConcurrentLinkedQueue<>();
	private static volatile boolean start = false;
	
	TimeoutRetryController() {}
	
	public static void start() {
		if (!start) {
			start = true;
			timeoutRetryGovernorExecutor.submit(new CheckTimeOutTask());
		}
	}

	public static void addRequest(AsyncCall asyncCall) {
		callQueue.offer(asyncCall);
		notifyGovernor();
	}

	public static void notifyGovernor() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
	public static void stop() {
		start = false;
		notifyGovernor();
		timeoutRetryGovernorExecutor.shutdown();
	}
	
	static class CheckTimeOutTask implements Runnable {
		long time2sleep = Long.MAX_VALUE; // to avoid meaningless spin, wait a little after every cycle. 
		@Override
		public void run() {
			while (!ServerStatus.offline.get()) {
				try {
					time2sleep = Long.MAX_VALUE;
					callQueue.forEach(asyncCall -> {
							if (asyncCall.isDone()) {
								callQueue.remove(asyncCall);
							} else {
								long timeLeft = asyncCall.retryMaybe();
								if (time2sleep > timeLeft) { // save the minimal left-time
									time2sleep = timeLeft;
								}
							}
					});
					if (time2sleep > 0) {
						synchronized (lock) {
							lock.wait(time2sleep);
						}
					}
				} catch (Exception e) {
					LogUtils.error(LOGGER, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
				}
			}
		}
	}
}



