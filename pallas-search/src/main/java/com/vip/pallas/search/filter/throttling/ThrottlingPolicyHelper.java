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
package com.vip.pallas.search.filter.throttling;

import com.google.common.util.concurrent.RateLimiter;
import com.vip.pallas.search.model.TemplateWithThrottling;
import com.vip.pallas.search.utils.SearchLogEvent;
import com.vip.pallas.utils.LogUtils;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ThrottlingPolicyHelper {

	private static final Logger logger = LoggerFactory.getLogger(ThrottlingPolicyHelper.class);

	protected static final Map<String, ThrottlingPolicy> throttlingPolicyMap = PlatformDependent
			.newConcurrentHashMap(32);

	public static void createPolicy(TemplateWithThrottling templateWithThrottling) {
		String restTemplateId = templateWithThrottling.getIndexName() + "_" + templateWithThrottling.getTemplateName();
		Integer threshold = templateWithThrottling.getThreshold();
		Integer maxBurstSecs = templateWithThrottling.getMaxBurstSecs();
		ThrottlingPolicy oldPolicy = throttlingPolicyMap.get(restTemplateId);
		if (threshold > 0) {
			if (null == oldPolicy || !oldPolicy.getThreshold().equals(threshold) || !oldPolicy
					.getMaxBurstSecs().equals(maxBurstSecs)) {
				ThrottlingPolicy policy = new ThrottlingPolicy(threshold, maxBurstSecs);
				throttlingPolicyMap.put(restTemplateId, policy);
				LogUtils.info(logger, SearchLogEvent.THROTTLING_EVENT, "ThrottlingPolicy of template [{}] changed from {} to {}",
						restTemplateId, oldPolicy, policy);
			}
		} else {
			// delete the throttling
			if (null != oldPolicy) {
				cleanPolicy(restTemplateId);
			}
		}
	}

	public static RateLimiter getRateLimiterByIndexAndTemplateName(String indexName, String templateName){
		String restTemplateId = indexName + "_" + templateName;
		return getRateLimiterByRestTemplateId(restTemplateId);
	}

	public static RateLimiter getRateLimiterByRestTemplateId(String templateName){
		ThrottlingPolicy oldPolicy = throttlingPolicyMap.get(templateName);
		if (null == oldPolicy){
			return null;
		}
		return oldPolicy.getRateLimiter();
	}

	public static void putPolicy(String key, ThrottlingPolicy policy) {
		ThrottlingPolicy oldPolicy = throttlingPolicyMap.get(key);

		if (!policy.equals(oldPolicy)) {
			throttlingPolicyMap.put(key, policy);
			LogUtils.info(logger, SearchLogEvent.THROTTLING_EVENT, "ThrottlingPolicy changed from {} to {}",
					oldPolicy, policy);
		}
	}

	public static void cleanPolicy(String key) {
		throttlingPolicyMap.remove(key);
	}

	/**
	 * only for test
	 */
	public static void cleanAllPolicy() {
		throttlingPolicyMap.clear();
	}
}
