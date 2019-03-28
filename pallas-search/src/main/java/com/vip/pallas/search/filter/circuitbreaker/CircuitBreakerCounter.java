package com.vip.pallas.search.filter.circuitbreaker;

/**
 * 熔断计数器
 */
public interface CircuitBreakerCounter {
	void increaseRequestCounter();

	void increaseFailedCounter();

	long countRequest();

	long countFailed();

	PercentageHolder calculateErrorPercentage();

	void setCircuitBreakerInterval(int interval);

	long getCircuitBreakerInterval();

	String getId();

	void cleanupFutureCounter();

	class PercentageHolder {
		public long totalRequests;
		public long failedRequests;
		public double percentage;

		PercentageHolder(long failedRequests, long totalRequests, double percentage) {
			this.failedRequests = failedRequests;
			this.totalRequests = totalRequests;
			this.percentage = percentage;
		}
	}
}
