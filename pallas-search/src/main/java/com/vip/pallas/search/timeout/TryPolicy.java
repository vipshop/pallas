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
