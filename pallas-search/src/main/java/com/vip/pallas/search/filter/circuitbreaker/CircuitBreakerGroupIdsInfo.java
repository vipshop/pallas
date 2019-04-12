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
  
