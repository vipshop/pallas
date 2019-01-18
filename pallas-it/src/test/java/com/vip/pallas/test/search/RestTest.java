package com.vip.pallas.test.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.vip.pallas.test.base.BaseSearchTest;

public class RestTest extends BaseSearchTest {
	
	@Test
	public void testUpdateRouting() throws Exception {
		assertThat(callRestApiAndReturnString("/_py/update_routing", "")).isEqualTo("rules updated.");
	}
	
	@Test
	public void test503() throws Exception {
		assertThat(callRestApiAndReturnResponse("/url/not_exist", "").getStatusLine().getStatusCode()).isGreaterThanOrEqualTo(400);
	}
	

}
