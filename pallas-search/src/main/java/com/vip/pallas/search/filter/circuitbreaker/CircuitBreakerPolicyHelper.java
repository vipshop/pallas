/**
 *
 */
package com.vip.pallas.search.filter.circuitbreaker;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import io.netty.util.internal.PlatformDependent;

/**
 * 维护服务的熔断设置的Helper类
 * 在RoutingCache类进行维护, 但凡开了动态分组的都默认开启
 *
 */
public class CircuitBreakerPolicyHelper {

	private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerPolicyHelper.class);

	// map的key是一组分片, indexName:preferNodes，记录每组分片服务的熔断配置
	public static final Map<String, CircuitBreakerPolicy> circuitBreakerPolicyMap = PlatformDependent.newConcurrentHashMap(32);

	public static void putPolicy(String key, String circuitBreakerPolicyContent) {

		CircuitBreakerPolicy newPolicy = parseContent(circuitBreakerPolicyContent);

		CircuitBreakerPolicy oldPolicy = CircuitBreakerPolicyHelper.circuitBreakerPolicyMap.get(key);

		if (!newPolicy.equals(oldPolicy)) {

			CircuitBreakerPolicyHelper.circuitBreakerPolicyMap.put(key, newPolicy);

			logger.info("【circuitBreaker】CircuitBreakerPolicy changed from {} to {}", oldPolicy, newPolicy);
		}
	}

	public static void cleanPolicy(String key) {
		circuitBreakerPolicyMap.remove(key);
	}

	private static CircuitBreakerPolicy parseContent(String circuitBreakerPolicyContent) {
		if (StringUtils.isEmpty(circuitBreakerPolicyContent)) {
			return new CircuitBreakerPolicy();
		}
		try {
			return JSON.parseObject(circuitBreakerPolicyContent, CircuitBreakerPolicy.class);
		} catch (Exception ex) {
			logger.error("【circuitBreaker】Wrong json format for the circuit breaker definition:"
					+ circuitBreakerPolicyContent, ex);
			return new CircuitBreakerPolicy();
		}
	}

}
