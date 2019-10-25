package com.vip.pallas.test.search;

import com.vip.pallas.search.filter.throttling.ThrottlingPolicy;
import com.vip.pallas.search.filter.throttling.ThrottlingPolicyHelper;
import com.vip.pallas.test.base.BaseSearchTest;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ThrottlingTest extends BaseSearchTest {

	@Test
	public void testThrottled() throws Exception {
		ThrottlingPolicyHelper.putPolicy("pallas-test-cluster_product_comment_product_comment_search", new ThrottlingPolicy(4,1));
		Map<String, String> header = new HashMap<>();
		header.put("business_code", "ittest");
		header.put("X-PALLAS-SEARCH-ES-DOMAIN", "pallas-test-cluster");
		header.put("X-PALLAS-SEARCH-TEMPLATE-ID", "product_comment_product_comment_search");

		String requestBody = "{\n" +
				"  \"id\": \"product_comment_product_comment_search\",\n" +
				"  \"params\": {\n" +
				" \"vendor_id\": 601000, " +
				" \"contenet\": \"鞋\"" +
				"  }\n" +
				"}";
		for (int i = 0; i < 4; i ++){
			System.out.println("i = " + i + ":" + System.currentTimeMillis());
			assertThat(callRestApiAndReturnResponse("127.0.0.1", SERVER_PORT, "/product_comment/_search/template", header, requestBody).getStatusLine().getStatusCode()).isEqualTo(200);
		}
		// the duration of the request is unpredictable, increase the request qps
		for (int i = 0; i < 6; i ++){
			System.out.println("i = " + i + ":" + System.currentTimeMillis());
			// make sure consume the permits
			callRestApiAndReturnResponse("127.0.0.1", SERVER_PORT, "/product_comment/_search/template", header, requestBody).getStatusLine().getStatusCode();
		}
		assertThat(callRestApiAndReturnResponse("127.0.0.1", SERVER_PORT, "/product_comment/_search/template", header, requestBody).getStatusLine().getStatusCode()).isEqualTo(429);
		Thread.sleep(1000L);
		assertThat(callRestApiAndReturnResponse("127.0.0.1", SERVER_PORT, "/product_comment/_search/template", header, requestBody).getStatusLine().getStatusCode()).isEqualTo(200);
	}

}
