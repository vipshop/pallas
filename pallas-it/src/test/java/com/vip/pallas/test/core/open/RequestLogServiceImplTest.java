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

package com.vip.pallas.test.core.open;

import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.RequestLog;
import com.vip.pallas.service.impl.RequestLogServiceImpl;
import com.vip.pallas.test.base.BaseEsTest;
import com.vip.pallas.utils.ElasticRestClient;
import com.vip.pallas.utils.IoUtil;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.After;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestLogServiceImplTest extends BaseEsTest {
	
	private RequestLogServiceImpl requestLogService = new RequestLogServiceImpl();
	RestClient client = ElasticRestClient.build("127.0.0.1:9200");
	
	@Test
	public void test() throws PallasException, InterruptedException {
		RequestLog log = new RequestLog();
		log.setBody("{}");
		String username = "it.test";
		log.setUsername(username);
		log.setMethod("POST");
		log.setPath("/whatever/");
		requestLogService.addRequestLog(client, log);
		TimeUnit.SECONDS.sleep(1);
		List<Map> loadHistory = requestLogService.loadHistory(client, username);
		assertThat(loadHistory.size()).isGreaterThanOrEqualTo(1);
	}
	
	@After
	public void deleteLog() {
		String query = IoUtil.loadFile("json/delete_it_request_log.json");
		HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
		try {
			Response indexResponse = client.performRequest("POST", ".pallas_request_log/log/_delete_by_query", Collections.<String, String> emptyMap(), entity);
			String response = IOUtils.toString(indexResponse.getEntity().getContent(), Charset.forName("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}