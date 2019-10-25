package com.vip.pallas.search.throttling;

import com.google.common.util.concurrent.RateLimiter;
import com.vip.pallas.search.filter.throttling.ThrottlingFilter;
import com.vip.pallas.search.filter.throttling.ThrottlingPolicy;
import com.vip.pallas.search.filter.throttling.ThrottlingPolicyHelper;
import com.vip.pallas.search.model.TemplateWithThrottling;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class ThrottlingPolicyTest {

	ThrottlingFilter filter = new ThrottlingFilter();

	@After
	public void clean(){
		ThrottlingPolicyHelper.cleanAllPolicy();
	}

	@Test
	public void testThrottlingPolicy() throws InterruptedException {
		ThrottlingPolicy policy = new ThrottlingPolicy(4, 2);
		// the init store size is 8, borrow 6
		boolean isThrottled = policy.getRateLimiter().tryAcquire(8);
		assertTrue(isThrottled);
		isThrottled = policy.getRateLimiter().tryAcquire(6);
		assertTrue(isThrottled);

		isThrottled = policy.getRateLimiter().tryAcquire(2);
		assertFalse(isThrottled);
		// 1.5 seconds for the former request; 0.5 seconds to store the permits
		Thread.sleep(2000L);

		// the follow 2 permits is storePermits
		isThrottled = policy.getRateLimiter().tryAcquire(1);
		assertTrue(isThrottled);
		isThrottled = policy.getRateLimiter().tryAcquire(1);
		assertTrue(isThrottled);

		// only the first time can borrow
		isThrottled = policy.getRateLimiter().tryAcquire(1);
		assertTrue(isThrottled);
		isThrottled = policy.getRateLimiter().tryAcquire(1);
		assertFalse(isThrottled);

		// save 4 permits again
		Thread.sleep(1000L);
		isThrottled = policy.getRateLimiter().tryAcquire(2);
		assertTrue(isThrottled);
		isThrottled = policy.getRateLimiter().tryAcquire(2);
		assertTrue(isThrottled);

		isThrottled = policy.getRateLimiter().tryAcquire(1);
		assertFalse(isThrottled);
		Thread.sleep(250L);
		isThrottled = policy.getRateLimiter().tryAcquire(1);
		assertTrue(isThrottled);
	}

	@Test
	public void testReplacePolicy(){
		TemplateWithThrottling templateWithThrottling = new TemplateWithThrottling();
		String indexName = "test_index";
		String template = "test_template";
		String clusterName = "test_cluster";
		templateWithThrottling.setIndexName(indexName);
		templateWithThrottling.setTemplateName(template);
		templateWithThrottling.setClusterName(clusterName);
		templateWithThrottling.setThreshold(4);
		templateWithThrottling.setMaxBurstSecs(1);
		ThrottlingPolicyHelper.createPolicy(templateWithThrottling);
		RateLimiter oldOne = ThrottlingPolicyHelper.getRateLimiterByIndexAndTemplateName(indexName, template);

		TemplateWithThrottling newConfig = new TemplateWithThrottling();
		newConfig.setIndexName("test_index");
		newConfig.setTemplateName("test_template");
		newConfig.setClusterName("test_cluster");
		newConfig.setThreshold(4);
		newConfig.setMaxBurstSecs(1);
		ThrottlingPolicyHelper.createPolicy(newConfig);

		RateLimiter newOne = ThrottlingPolicyHelper.getRateLimiterByIndexAndTemplateName(indexName, template);
		assertSame(oldOne, newOne);

		newConfig.setThreshold(10);
		ThrottlingPolicyHelper.createPolicy(newConfig);

		newOne = ThrottlingPolicyHelper.getRateLimiterByIndexAndTemplateName(indexName, template);

		assertEquals(newOne.getRate(), 10, 0);
	}

	@Test
	public void testCleanPolicy(){
		TemplateWithThrottling templateWithThrottling = new TemplateWithThrottling();
		String indexName = "test_index";
		String template = "test_template";
		String clusterName = "test_cluster";
		templateWithThrottling.setIndexName(indexName);
		templateWithThrottling.setTemplateName(template);
		templateWithThrottling.setClusterName(clusterName);
		templateWithThrottling.setThreshold(4);
		templateWithThrottling.setMaxBurstSecs(1);
		ThrottlingPolicyHelper.createPolicy(templateWithThrottling);
		RateLimiter oldOne = ThrottlingPolicyHelper.getRateLimiterByIndexAndTemplateName(indexName, template);
		assertEquals(oldOne.getRate(), 4, 0);

		TemplateWithThrottling newConfig = new TemplateWithThrottling();
		newConfig.setIndexName("test_index");
		newConfig.setTemplateName("test_template");
		newConfig.setClusterName("test_cluster");
		newConfig.setThreshold(0);
		newConfig.setMaxBurstSecs(1);
		ThrottlingPolicyHelper.createPolicy(newConfig);

		RateLimiter newOne = ThrottlingPolicyHelper.getRateLimiterByIndexAndTemplateName(indexName, template);

		assertNull(newOne);
	}

}
