package com.vip.pallas.search.filter.throttling;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Objects;

public class ThrottlingPolicy {
	private RateLimiter rateLimiter;
	private Integer threshold;
	private Integer maxBurstSecs;

	public ThrottlingPolicy(Integer threshold, Integer maxBurstSecs) {
		this.rateLimiter = RateLimiterUtil.create(threshold, maxBurstSecs);
		this.threshold = threshold;
		this.maxBurstSecs = maxBurstSecs;
	}

	public RateLimiter getRateLimiter() {
		return rateLimiter;
	}

	public void setRateLimiter(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	public Integer getMaxBurstSecs() {
		return maxBurstSecs;
	}

	public void setMaxBurstSecs(Integer maxBurstSecs) {
		this.maxBurstSecs = maxBurstSecs;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ThrottlingPolicy))
			return false;
		ThrottlingPolicy that = (ThrottlingPolicy) o;
		return Objects.equals(threshold, that.threshold) && Objects.equals(maxBurstSecs, that.maxBurstSecs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(threshold, maxBurstSecs);
	}

	@Override
	public String toString() {
		return "ThrottlingPolicy{" + "rateLimiter=" + rateLimiter + ", threshold=" + threshold + ", maxBurstSecs="
				+ maxBurstSecs + '}';
	}
}
