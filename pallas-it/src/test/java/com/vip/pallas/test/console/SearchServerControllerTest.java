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

package com.vip.pallas.test.console;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.test.base.BaseSpringEsTest;

public class SearchServerControllerTest extends BaseSpringEsTest {

	static final String CLUSTER = System.currentTimeMillis() + "-cluster";
	static final String IPPORT = System.currentTimeMillis()+"";
	static String AFTER_INSERT_ID = "";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testClusterManage() throws Exception {
		try {

			//upsert
			JSONObject jo = new JSONObject();
			jo.fluentPut("cluster", CLUSTER).fluentPut("ipport", IPPORT).fluentPut("info", "{}");
			// insert
			assertThat(callRestApi("/ss/upsert.json", jo.toJSONString())).isNull();
			
			// get clusters
			assertThat(callRestApi("/ss/clusters.json", "{}").toString()).contains(CLUSTER);
			
			// update
			assertThat(callRestApi("/ss/upsert.json", jo.toJSONString())).isNull(); // update
			
			// find
			String listStr = callGetApiAsString("/ss/find.json?currentPage=1&pageSize=1&selectedCluster="+ CLUSTER);
			assertThat(listStr).contains(CLUSTER).contains(IPPORT);
			
			System.out.println(callGetApiAsString("/ss/find.json?selectedCluster=it-test").toString());
			
			
			
			// get id
			AFTER_INSERT_ID = StringUtils.substringBetween(listStr, "id\":", ",");
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	@AfterClass
	public static void cleanData() throws IOException {
		if (StringUtils.isNotBlank(AFTER_INSERT_ID)) {
			assertThat(callRestApi("/ss/delete.json", "{\"id\": \"" + AFTER_INSERT_ID + "\"}")).isNull();
		}
	}
}