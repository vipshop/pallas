package com.vip.pallas.search.filter.circuitbreaker;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于正常状态的滑动窗口Counter.
 * 
 * 循环数组，每秒一个桶.
 * 
 * 依赖SlidingTimeWindowCounterResetScheduler将不在计算范围内的桶重新置0.
 */
public final class SlidingTimeWindowCounter implements CircuitBreakerCounter {
	private static final int RING_WINDOW_BUFFER = 30;
	
	private static final int CLEAN_UP_BUFFER = 10;
	
	private AtomicInteger[] requestCounter;

	private AtomicInteger[] failedCounter;

	private int window; //计算窗口
	private int ringWindow; //整个循环数组窗口（留有Buffer，以供Scheduler将无用的桶置0）
	
	private String id;

	private long initCounterTimeInSecond;

	public SlidingTimeWindowCounter(int window, String id) {
		this.id = id;
		this.window = window;
		this.ringWindow = window + RING_WINDOW_BUFFER;

		requestCounter = new AtomicInteger[ringWindow];
		failedCounter = new AtomicInteger[ringWindow];
		for (int i = 0; i < ringWindow; i++) {
			requestCounter[i] = new AtomicInteger(0);
			failedCounter[i] = new AtomicInteger(0);
		}

		initCounterTimeInSecond = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
	}

	@Override
	public void increaseRequestCounter() {
		int index = getIndex();
		requestCounter[index].incrementAndGet();
	}

	@Override
	public void increaseFailedCounter() {
		int index = getIndex();
		failedCounter[index].incrementAndGet();
	}

	@Override
	public long countRequest() {
		return countTotal(requestCounter);
	}

	@Override
	public long countFailed() {
		return countTotal(failedCounter);
	}
	
	private long countTotal(AtomicInteger[] caculateCounter){
		int currentIndex = getIndex();
		
		long sum = 0;
		
		for (int i = 0; i < window; i++) {
			int index = ((currentIndex + ringWindow) -i) % this.ringWindow;
			sum += caculateCounter[index].get();
		}
		return sum;
	}

	@Override
	public PercentageHolder calculateErrorPercentage() {
		long totalRequests = countRequest();
		long failedRequests = countFailed();
		double percentage = totalRequests != 0 ? (failedRequests * 100.0) / totalRequests : 0;
		return new PercentageHolder(failedRequests, totalRequests, percentage);
	}

	@Override
	public void setCircuitBreakerInterval(int interval) {
		this.window = interval;
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
		int currentIndex = getIndex();
		for (int i = 1 ; i <= CLEAN_UP_BUFFER; i++) {
			int index = (currentIndex + i) % this.ringWindow;
			requestCounter[index].set(0);
			failedCounter[index].set(0);
		}
	}
	
	private int getIndex() {
		long currentTimeInSecond = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
		long passTimeInSecond = currentTimeInSecond - initCounterTimeInSecond;

		return (int) Math.abs(passTimeInSecond % this.ringWindow);// 取模
	}

}
