package com.vip.pallas.search.timeout;

public class TryPolicy {
	
	private final int totalCountIncludedFirstTime;
	private int timeoutMillis;
	
	public TryPolicy(int totalCountIncludedFirstTime, int timeoutMillis) {
		this.totalCountIncludedFirstTime = totalCountIncludedFirstTime;
		this.timeoutMillis = timeoutMillis;
	}

	public boolean allowRetry(int retryCount) {
		return retryCount <= totalCountIncludedFirstTime;
	}

	public int getTimeoutMillis() {
		return timeoutMillis;
	}

	public int getTotalCountIncludedFirstTime() {
		return totalCountIncludedFirstTime;
	}

}
