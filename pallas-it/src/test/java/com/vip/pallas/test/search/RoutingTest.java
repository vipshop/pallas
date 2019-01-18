package com.vip.pallas.test.search;

import com.vip.pallas.test.base.BaseSearchTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RoutingTest extends BaseSearchTest {

	@Test
	public void testWithClusterLevel() throws Exception {
		Map<String, String> header = new HashMap<>();
		header.put("business_code", "ittest");
		header.put("X-PALLAS-SEARCH-ES-DOMAIN", "pallas-test-cluster");

		String requestBody = "{\n" +
                "  \"id\": \"product_comment_product_comment_search\",\n" +
                "  \"params\": {\n" +
                " \"vendor_id\": 601000, " +
                " \"contenet\": \"鞋\"" +
                "  }\n" +
                "}";

        assertThat(callRestApiAndReturnResponse("127.0.0.1", SERVER_PORT, "/product_comment/_search/template", header, requestBody).getStatusLine().getStatusCode()).isEqualTo(200);
	}
}
