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

package com.vip.pallas.search.filter.circuitbreaker;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vip.pallas.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.thread.PallasThreadFactory;

/**
 * 清除已经无用的Counter，避免内存泄漏
 */
public class CircuitBreakerCounterCleanupScheduler {
	private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerCounterCleanupScheduler.class);

	// 让值可以修改，only for test
	public static int PALLAS_SEARCH_CIRCUITBREAKER_CLEANUP_INTERVAL = Integer
			.getInteger("pallas.search.circuitbreaker.cleanup.interval", 24 * 3600);

	private static ScheduledExecutorService scheduledExecutor;
	private static CircuitBreakerService circuitBreakerService;

	public static void init(CircuitBreakerService theCircuitBreakerService) {
		circuitBreakerService = theCircuitBreakerService;
		
		if (PALLAS_SEARCH_CIRCUITBREAKER_CLEANUP_INTERVAL > 0) {
			scheduledExecutor = Executors
					.newSingleThreadScheduledExecutor(new PallasThreadFactory("Pallas-CircuitBreakerCounterCleanupScheduler"));
			scheduledExecutor.scheduleAtFixedRate(
					new CircuitBreakerCounterCleanupTask(circuitBreakerService),
					PALLAS_SEARCH_CIRCUITBREAKER_CLEANUP_INTERVAL, PALLAS_SEARCH_CIRCUITBREAKER_CLEANUP_INTERVAL,
					TimeUnit.SECONDS);
	
		}
	}
	
	/**
	 * just for test invoke
	 */
	public static void rescheduler() {
		try {
			if (PALLAS_SEARCH_CIRCUITBREAKER_CLEANUP_INTERVAL > 0) {
				scheduledExecutor.shutdown();
				scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS);
			}
		} catch (Exception ex) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, "【circuitBreaker】CircuitBreakerCleanupScheduler reschedule has error!", ex);
		}
		init(circuitBreakerService);
	}

	private static final class CircuitBreakerCounterCleanupTask implements Runnable {
		private CircuitBreakerService myCircuitBreakerService;

		private CircuitBreakerCounterCleanupTask(CircuitBreakerService theCircuitBreakerService) {
			this.myCircuitBreakerService = theCircuitBreakerService;
		}

		@Override
		public void run() {
			try {
				long startTime = System.currentTimeMillis();
				String beginCleanUpMsg = "Begin CircuitBreakerCounterCleanupTask cleanup counter";
				LogUtils.info(logger, SearchLogEvent.NORMAL_EVENT,beginCleanUpMsg);
				int count = 0;
				for (Iterator<Map.Entry<String, CircuitBreakerCounter>> it = myCircuitBreakerService
						.getGroupInvokeCounterMap().entrySet().iterator(); it.hasNext();) {
					Map.Entry<String, CircuitBreakerCounter> entry = it.next();
					CircuitBreakerCounter counter = entry.getValue();
					if (isNeedCleanUp(counter)) {
						it.remove();
						count++;
					}
				}
				// myCircuitBreakerService.getServiceInvokeHashKeysMap().clear();// 每天清一次服务和hash key集合的映射，保证超过服务的hash
				// key阈值个数的服务可以恢复熔断功能
				LogUtils.info(logger, SearchLogEvent.NORMAL_EVENT,
						"【circuitBreaker】Finish CircuitBreakerCounterCleanupTask cleanup counter count : {} , used time : {} ms",
						count, System.currentTimeMillis() - startTime);
			} catch (Exception ex) {
				LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT,"【circuitBreaker】CircuitBreakerCounterCleanupTask has error!", ex);
			}
		}

		// 周期内无请求，可清除
		private static boolean isNeedCleanUp(CircuitBreakerCounter counter) {
			return counter.countRequest() == 0;
		}
	}
}
