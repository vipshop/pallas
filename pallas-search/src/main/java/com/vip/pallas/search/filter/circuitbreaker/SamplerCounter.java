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

import java.util.concurrent.atomic.AtomicLong;

/**
 * 用于半开状态的Counter, 只放若干个请求，进行一一计数.
 */
public final class SamplerCounter implements CircuitBreakerCounter {
	public static final double HALF_OPEN_ERROR_RATE_DISCOUNT = 0.8;

	private final AtomicLong requestCounter;
	private final AtomicLong failedCounter;
	
	private String id;
	private int window;

	public SamplerCounter(int window, String id) {
		this.window = window;
		this.id = id;
		this.requestCounter = new AtomicLong();
		this.failedCounter = new AtomicLong();
	}

	@Override
	public void increaseRequestCounter() {
		requestCounter.incrementAndGet();
	}

	@Override
	public void increaseFailedCounter() {
		failedCounter.incrementAndGet();
	}

	@Override
	public long countRequest() {
		return requestCounter.get();
	}

	@Override
	public long countFailed() {
		return failedCounter.get();
	}

	@Override
	public PercentageHolder calculateErrorPercentage() {
		long failedRequests = countFailed();
		long countRequests = countRequest();
		double percentage = countRequests == 0 ? 0
				: (failedRequests * 100.0 / countRequests) / HALF_OPEN_ERROR_RATE_DISCOUNT;

		return new PercentageHolder(failedRequests, countRequests, percentage);
	}

	@Override
	public void setCircuitBreakerInterval(int interval) {
		// do nothing
	}

	@Override
	public long getCircuitBreakerInterval() {
		return this.window;
	}

	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public void cleanupFutureCounter() {
	}
}
