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

package com.vip.pallas.client.thread;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanRestClientTask implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(CleanRestClientTask.class);

	private static Object lock = new Object();

	private static final ConcurrentLinkedQueue<OldRestClientWrapper> OLD_CLIET_Q = new ConcurrentLinkedQueue<>();

	public CleanRestClientTask() {
	}

	@Override
	public void run() {
		while (true) {
			long time2sleep = Long.MAX_VALUE;
			for (OldRestClientWrapper oldRestClientWrapper : OLD_CLIET_Q) {
				try {
					long currentTimeMillis = System.currentTimeMillis();
					if (currentTimeMillis >= oldRestClientWrapper.millisTime2kill) {
						if (oldRestClientWrapper.restClient != null) { //NOSONAR
							oldRestClientWrapper.restClient.close();
						}
						OLD_CLIET_Q.remove(oldRestClientWrapper);
						logger.info("rest client with old hosts closed.");
					} else {
						long timeLeft = oldRestClientWrapper.millisTime2kill - currentTimeMillis;
						if (time2sleep > timeLeft) { // save the minimal left-time NOSONAR
							time2sleep = timeLeft;
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (time2sleep > 0) {
				synchronized (lock) {
					try {
						lock.wait(time2sleep);
					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	public static void addClient(long waitMills, RestClient restClient) {
		if (restClient != null) {
			OLD_CLIET_Q.offer(new OldRestClientWrapper(waitMills, restClient));
			logger.info("a rest client ready to close after {}", waitMills);
			notifyGovernor();
		}
	}

	public static void notifyGovernor() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	static class OldRestClientWrapper {
		public long millisTime2kill;
		public RestClient restClient;

		public OldRestClientWrapper(long waitMillis, RestClient restClient) {
			this.millisTime2kill = System.currentTimeMillis() + waitMillis;
			this.restClient = restClient;
		}
	}

}