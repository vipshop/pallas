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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.vip.pallas.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.filter.circuitbreaker.CircuitBreakerCounter.PercentageHolder;
import com.vip.pallas.thread.PallasThreadFactory;
import com.vip.vjtools.vjkit.collection.type.ConcurrentHashSet;

import io.netty.util.internal.PlatformDependent;

/**
 * 熔断逻辑
 */
public class CircuitBreakerService {

	private static class ServiceCircuitBreakerHolder {
		static final CircuitBreakerService instance = new CircuitBreakerService();
	}

	private static final int PERCENTAGE_TO_DECIMALS = 100;

	private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerService.class);

	// map的key是index:shardNodes的hash值
	private ConcurrentHashMapV8<String, CircuitBreakerCounter> groupInvokeCounterMap = new ConcurrentHashMapV8<>();

	// map的key是index:shardNodes
	private ConcurrentMap<String, GroupScheduledFuture> scheduledFutureMap = PlatformDependent
			.newConcurrentHashMap();

	private ConcurrentHashSet<String> openGroupsList = new ConcurrentHashSet<String>();

	private ConcurrentHashSet<String> halfOpenGroupsList = new ConcurrentHashSet<String>();

	private ScheduledThreadPoolExecutor scheduledExecutor = (ScheduledThreadPoolExecutor) Executors
			.newScheduledThreadPool(1, new PallasThreadFactory("Pallas-CircuitBreakerHalfOpenScheduler"));

	private CircuitBreakerService() {
		SlidingTimeWindowCounterResetScheduler.init(this);
		CircuitBreakerCounterCleanupScheduler.init(this);
	}

	public static CircuitBreakerService getInstance() {
		return ServiceCircuitBreakerHolder.instance;
	}

	/**
	 * 正常调用前，调用本方法 是否开启熔断在调用此方法前判断
	 */
	public void increaseServiceRequestCounter(String id) {
		CircuitBreakerPolicy circuitBreakerPolicy = CircuitBreakerPolicyHelper.circuitBreakerPolicyMap.get(id);
		// 如果设置熔断
		if (circuitBreakerPolicy != null) {

			CircuitBreakerCounter counter = increaseRequestCounterAndGet(id, circuitBreakerPolicy);

			// 若half-open 状态下的服务instance被调用次数超过取样的sample数，没有达到失败阈值被再次熔断，则从half-open服务列表中去掉,回归close状态
			if (isServerInHalfOpen(id) && counter.countRequest() > circuitBreakerPolicy.getRecoverySampleVolume()) {
				LogUtils.info(logger, SearchLogEvent.NORMAL_EVENT, "【circuitBreaker】circuit break move from half open to close, indexName:preferNodes = " + id);
				if (getHalfOpenGroupsList().contains(id)) {
					getHalfOpenGroupsList().remove(id);
				}
				// 当server从half-open移走到close状态的时候，需要清空统计数据
				groupInvokeCounterMap.remove(id);
			}
		}
	}

	/**
	 * 调用异常后，调用此方法.
	 *
	 * 1.增加失败次数的counter,2.计算错误百分比 3. 若超过熔断策略，则加到Open Circuit break Servers..
	 */
	public void handleFailedRequestCounter(String id) {

		CircuitBreakerPolicy circuitBreakerPolicy = CircuitBreakerPolicyHelper.circuitBreakerPolicyMap.get(id);
		// 如果设置熔断
		if (circuitBreakerPolicy != null) {

			CircuitBreakerCounter counter = increaseFailedCounterAndGet(id, circuitBreakerPolicy);

			if (isServerInHalfOpen(id)) {
				// 若错误数达标，不需要等到sample数达标才熔断，目的是fail fast
				double failThreshold = circuitBreakerPolicy.getErrorPercentage()
						* SamplerCounter.HALF_OPEN_ERROR_RATE_DISCOUNT * circuitBreakerPolicy.getRecoverySampleVolume()
						/ PERCENTAGE_TO_DECIMALS;
				if (counter.countFailed() >= failThreshold) {
					moveToOpenCircuitBreakerServers(id, CircuitBreakStatus.HALF_OPEN, counter.calculateErrorPercentage());
				}
			} else {
				// 若是针对Close Servers的计算，则需要计算最低访问次数和错误率达到错误百分比

				if (counter.countRequest() >= circuitBreakerPolicy.getRequestVolumeThreshold()) {
					PercentageHolder errorPercentage = counter.calculateErrorPercentage();

					if (errorPercentage.percentage >= circuitBreakerPolicy.getErrorPercentage()) {
						moveToOpenCircuitBreakerServers(id, CircuitBreakStatus.CLOSED, errorPercentage);
					}
				}
			}
		}
	}


	/**
	 * 定时任务调用，定时将Open的Server放回Half-Open状态
	 */
	public void moveOpenCircuitBreakerServerToHalfOpen(String id) {
		LogUtils.info(logger, SearchLogEvent.NORMAL_EVENT, "【circuitBreaker】circuit break move from open to half-open status, id = " + id);

		if (getOpenGroupsList().contains(id)) {
			// remove server ip from open circuit breaker
			getOpenGroupsList().remove(id);

			// put server ip into half open circuit breaker
			if (!getHalfOpenGroupsList().contains(id)) {
				getHalfOpenGroupsList().add(id);
			}
		}
		scheduledFutureMap.remove(id);
	}

	CircuitBreakerCounter increaseRequestCounterAndGet(String id, CircuitBreakerPolicy circuitBreakerPolicy) {
		CircuitBreakerCounter counter = getOrCreateCounter(id, circuitBreakerPolicy);
		counter.increaseRequestCounter();
		return counter;
	}

	private CircuitBreakerCounter increaseFailedCounterAndGet(String id, CircuitBreakerPolicy circuitBreakerPolicy) {
		CircuitBreakerCounter counter = getOrCreateCounter(id, circuitBreakerPolicy);
		counter.increaseFailedCounter();
		return counter;
	}

	private CircuitBreakerCounter getOrCreateCounter(String id, final CircuitBreakerPolicy circuitBreakerPolicy) {

		CircuitBreakerCounter counter = groupInvokeCounterMap.get(id);

		if (!hasCircuitBreakerCounter(circuitBreakerPolicy.getInterval(), counter)) {
			counter = groupInvokeCounterMap.computeIfAbsent(id,
					new ConcurrentHashMapV8.Fun<String, CircuitBreakerCounter>() {

				@Override
				public CircuitBreakerCounter apply(String id) {
					return newCircuitBreakerCounter(id, circuitBreakerPolicy.getInterval());
				}
			});
		}
		return counter;
	}

	private static boolean hasCircuitBreakerCounter(int policyWindow, CircuitBreakerCounter counter) {
		return counter != null && policyWindow == counter.getCircuitBreakerInterval();
	}

	private CircuitBreakerCounter newCircuitBreakerCounter(String id, int circuitBreakerInterval) {
		if (circuitBreakerInterval == 0) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, "【circuitBreaker】circuit breaker interval can't set to 0");
		}

		if (isServerInHalfOpen(id)) {
			return new SamplerCounter(circuitBreakerInterval, id);
		} else {
			return new SlidingTimeWindowCounter(circuitBreakerInterval, id);
		}
	}

	private boolean isServerInHalfOpen(String id) {
		return getHalfOpenGroupsList().contains(id);
	}

	private void moveToOpenCircuitBreakerServers(String id, CircuitBreakStatus previousStatus,
			PercentageHolder percentage) {
		LogUtils.info(logger, SearchLogEvent.NORMAL_EVENT, "【circuitBreaker】circuit break move from {} to {} status, id = {}", previousStatus.getTitle(),
				CircuitBreakStatus.OPEN.getTitle(), id);

		// 增加到open服务列表
		if (!getOpenGroupsList().contains(id)) {
			getOpenGroupsList().add(id);
		}

		// 同时把half-open的服务清除
		if (getHalfOpenGroupsList().contains(id)) {
			getHalfOpenGroupsList().remove(id);
		}

		// 当server移到open状态的时候，需要清空之前的计数数据
		groupInvokeCounterMap.remove(id);

		// 定义稍后将其移到HalfOpen的一次性任务
		CircuitBreakerPolicy circuitBreakerPolicy = CircuitBreakerPolicyHelper.circuitBreakerPolicyMap.get(id);
		ScheduledFuture scheduledFuture = scheduledExecutor.schedule(new MoveOpenToHalfOpenTask(id),
				circuitBreakerPolicy.getSleepWindow(), TimeUnit.SECONDS);
		scheduledFutureMap.put(id, new GroupScheduledFuture(id, scheduledFuture));
	}

	/**
	 * 重置，目前主要用于IT
	 */
	public void reset() {
		scheduledExecutor.getQueue().clear();
		groupInvokeCounterMap.clear();
		getOpenGroupsList().clear();
		getHalfOpenGroupsList().clear();
	}

	public synchronized void clear(String id) {
		//清除open的
		getOpenGroupsList().remove(id);
		//清除half open的
		getHalfOpenGroupsList().remove(id);

		//清除count
		groupInvokeCounterMap.remove(id);
		//清除定时任务
		GroupScheduledFuture groupScheduledFuture = scheduledFutureMap.get(id);
		if (groupScheduledFuture != null) {
			groupScheduledFuture.getScheduledFuture().cancel(false);
			scheduledFutureMap.remove(id);
		}
	}

	public class MoveOpenToHalfOpenTask implements Runnable {
		private String id;

		public MoveOpenToHalfOpenTask(String id) {
			this.id = id;

		}

		@Override
		public void run() {
			try {
				moveOpenCircuitBreakerServerToHalfOpen(id);
			} catch (Exception ex) {
				LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, "【circuitBreaker】MoveOpenToHalfOpenTask has error", ex);
			}
		}

	}


	public class GroupScheduledFuture {
		private ScheduledFuture scheduledFuture;
		private String id;

		public GroupScheduledFuture(String id, ScheduledFuture scheduledFuture) {
			this.scheduledFuture = scheduledFuture;
			this.id = id;
		}

		public ScheduledFuture getScheduledFuture() {
			return scheduledFuture;
		}

		public String getId() {
			return id;
		}
	}


	public ConcurrentHashMapV8<String, CircuitBreakerCounter> getGroupInvokeCounterMap() {
		return groupInvokeCounterMap;
	}

	public ConcurrentHashSet<String> getOpenGroupsList() {
		return openGroupsList;
	}

	public ConcurrentHashSet<String> getHalfOpenGroupsList() {
		return halfOpenGroupsList;
	}

}
