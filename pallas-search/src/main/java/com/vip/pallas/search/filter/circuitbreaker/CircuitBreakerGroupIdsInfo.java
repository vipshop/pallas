/**
 * vips Inc.
 * Copyright (c) 2018 All Rights Reserved.
 */   
package com.vip.pallas.search.filter.circuitbreaker;

import java.util.Set;

import com.vip.vjtools.vjkit.collection.type.ConcurrentHashSet;

public class CircuitBreakerGroupIdsInfo {

	private Set<String> serviceInvokeHashKeys = new ConcurrentHashSet<>();// index:shardNodes的值集合
	
	private volatile boolean throttled = false;// 是否已经到达阈值

	public Set<String> getServiceInvokeHashKeys() {
		return serviceInvokeHashKeys;
	}

	public boolean isThrottled() {
		return throttled;
	}

	public void setThrottled(boolean throttled) {
		this.throttled = throttled;
	}
	
}
  