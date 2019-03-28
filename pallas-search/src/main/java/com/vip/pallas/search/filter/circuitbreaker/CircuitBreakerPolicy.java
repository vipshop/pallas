
package com.vip.pallas.search.filter.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircuitBreakerPolicy {
	private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerPolicy.class);

	public static final int CIRCUIT_BREAKER_ERROR_PERCENTAGE_DEFAULT_VALUE = 60;
	public static final int CIRCUIT_BREAKER_SILDING_WINDOW_DEFAULT_VALUE = 60;
	public static final int CIRCUIT_BREAKER_THRESHOLD_DEFAULT_VALUE = 10;
	public static final int CIRCUIT_BREAKER_SLEEP_WINDOW_DEFAULT_VALUE = 120;
	public static final int CIRCUIT_BREAKER_SAMPLE_DEFAULT_VALUE = 10;

	private static final int CIRCUIT_BREAKER_SILDING_WINDOW_MIN_VALUE = 5; // 5 second
	private static final int CIRCUIT_BREAKER_SILDING_WINDOW_MAX_VALUE = 600; // 10 min

	private static final int CIRCUIT_BREAKER_ERROR_PERCENTAGE_MIN_VALUE = 10; // 10%
	private static final int CIRCUIT_BREAKER_ERROR_PERCENTAGE_MAX_VALUE = 100; // 100%

	private static final int CIRCUIT_BREAKER_SLEEP_WINDOW_MIN_VALUE = 1; // 1 second
	private static final int CIRCUIT_BREAKER_SLEEP_WINDOW_MAX_VALUE = 600; // 10 min

	private int errorPercentage = CIRCUIT_BREAKER_ERROR_PERCENTAGE_DEFAULT_VALUE;
	private int interval = CIRCUIT_BREAKER_SILDING_WINDOW_DEFAULT_VALUE;
	private int requestVolumeThreshold = CIRCUIT_BREAKER_THRESHOLD_DEFAULT_VALUE;
	private int sleepWindow = CIRCUIT_BREAKER_SLEEP_WINDOW_DEFAULT_VALUE;
	private int recoverySampleVolume = CIRCUIT_BREAKER_SAMPLE_DEFAULT_VALUE;

	public int getErrorPercentage() {
		return errorPercentage;
	}

	public void setErrorPercentage(int circuitBreakerErrorPercentage) {
		if (circuitBreakerErrorPercentage < CIRCUIT_BREAKER_ERROR_PERCENTAGE_MIN_VALUE) {
			logger.error("熔断设置中的错误率必须大于10%");
			this.errorPercentage = CIRCUIT_BREAKER_ERROR_PERCENTAGE_MIN_VALUE;
		} else if (circuitBreakerErrorPercentage > CIRCUIT_BREAKER_ERROR_PERCENTAGE_MAX_VALUE) {
			logger.error("熔断设置中的错误率必须小于100%");
			this.errorPercentage = CIRCUIT_BREAKER_ERROR_PERCENTAGE_MAX_VALUE;
		} else {
			this.errorPercentage = circuitBreakerErrorPercentage;
		}
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int circuitBreakerInterval) {
		if (circuitBreakerInterval < CIRCUIT_BREAKER_SILDING_WINDOW_MIN_VALUE) {
			logger.error("熔断设置中的Interval时间必须大于5秒");
			this.interval = CIRCUIT_BREAKER_SILDING_WINDOW_MIN_VALUE;
		} else if (circuitBreakerInterval > CIRCUIT_BREAKER_SILDING_WINDOW_MAX_VALUE) {
			logger.error("熔断设置中的Interval时间必须小于600秒");
			this.interval = CIRCUIT_BREAKER_SILDING_WINDOW_MAX_VALUE;
		} else {
			this.interval = circuitBreakerInterval;
		}

	}

	public int getRequestVolumeThreshold() {
		return requestVolumeThreshold;
	}

	public void setRequestVolumeThreshold(int circuitBreakerRequestVolumeThreshold) {
		this.requestVolumeThreshold = circuitBreakerRequestVolumeThreshold < 0 ? CIRCUIT_BREAKER_THRESHOLD_DEFAULT_VALUE : circuitBreakerRequestVolumeThreshold;
	}

	public int getSleepWindow() {
		return sleepWindow;
	}

	public void setSleepWindow(int circuitBreakerSleepWindow) {
		if (circuitBreakerSleepWindow < CIRCUIT_BREAKER_SLEEP_WINDOW_MIN_VALUE) {
			logger.error("熔断设置中的Sleep Window时间必须大于5秒");
			this.sleepWindow = CIRCUIT_BREAKER_SLEEP_WINDOW_MIN_VALUE;
		} else if (circuitBreakerSleepWindow > CIRCUIT_BREAKER_SLEEP_WINDOW_MAX_VALUE) {
			logger.error("熔断设置中的Sleep Window时间必须小于600秒");
			this.sleepWindow = CIRCUIT_BREAKER_SLEEP_WINDOW_MAX_VALUE;
		} else {
			this.sleepWindow = circuitBreakerSleepWindow;
		}
	}

	public int getRecoverySampleVolume() {
		return recoverySampleVolume;
	}

	public void setRecoverySampleVolume(int recoverySampleVolume) {
		this.recoverySampleVolume = recoverySampleVolume < 0 ? CIRCUIT_BREAKER_SAMPLE_DEFAULT_VALUE : recoverySampleVolume;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CircuitBreakerPolicy) {
			CircuitBreakerPolicy policy = (CircuitBreakerPolicy) obj;

			return this.errorPercentage == policy.errorPercentage && this.interval == policy.interval
					&& this.requestVolumeThreshold == policy.requestVolumeThreshold
					&& this.sleepWindow == policy.sleepWindow
					&& this.recoverySampleVolume == policy.recoverySampleVolume;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + errorPercentage;
		result = prime * result + interval;
		result = prime * result + requestVolumeThreshold;
		result = prime * result + sleepWindow;
		result = prime * result + recoverySampleVolume;

		return result;
	}
}
