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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vip.pallas.search.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.thread.PallasThreadFactory;

/**
 * 为滑动窗口Counter的循环数组重新置0的任务
 */
public class SlidingTimeWindowCounterResetScheduler {
	private static final Logger logger = LoggerFactory.getLogger(SlidingTimeWindowCounterResetScheduler.class);

	private static final int INTERVAL = 5;

	public static void init(CircuitBreakerService circuitBreakerService) {
		ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(
				new PallasThreadFactory("Pallas-CircuitBreakerSlidingWindowCounterResetScheduler"));
		scheduledExecutor.scheduleAtFixedRate(new ResetSlidingWindowTask(circuitBreakerService), 1, INTERVAL,
				TimeUnit.SECONDS);
	}

	private static final class ResetSlidingWindowTask implements Runnable {
		private CircuitBreakerService circuitBreakerService;

		private ResetSlidingWindowTask(CircuitBreakerService circuitBreakerService) {
			this.circuitBreakerService = circuitBreakerService;
		}

		@Override
		public void run() {
			try {
				for (CircuitBreakerCounter counter : circuitBreakerService.getGroupInvokeCounterMap().values()) {
					// 若是SlidingTimeWindowCounter and isServiceCircuitBreakerOpen,才需要清理数据
					if (counter instanceof SlidingTimeWindowCounter && isServiceCircuitBreakerOpen(counter)) {
						counter.cleanupFutureCounter();
					}
				}
			} catch (Exception ex) {
				LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, "【circuitBreaker】SlidingTimeWindowCounterResetTask has error!", ex);
			}
		}

		private static boolean isServiceCircuitBreakerOpen(CircuitBreakerCounter counter) {
			return CircuitBreakerPolicyHelper.circuitBreakerPolicyMap.containsKey(counter.getId());
		}
	}
}
