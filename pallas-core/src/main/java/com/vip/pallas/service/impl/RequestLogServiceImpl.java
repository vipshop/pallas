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

package com.vip.pallas.service.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vip.pallas.mybatis.entity.RequestLog;
import com.vip.pallas.service.RequestLogService;
import com.vip.pallas.utils.IoUtil;
import com.vip.pallas.utils.JsonUtil;

@Service
public class RequestLogServiceImpl implements RequestLogService {
	private static final String INDEX_NAME = "/.pallas_request_log";
	private static final String TYPE_NAME = "log";
	
	private static Logger logger = LoggerFactory.getLogger(RequestLogServiceImpl.class);

	private boolean exists(RestClient restClient) throws IOException {
		HttpEntity entity = new NStringEntity("{}", ContentType.APPLICATION_JSON);
		try {
			restClient.performRequest("GET", INDEX_NAME, Collections.<String, String> emptyMap(), entity);
			return true;
		} catch (ResponseException e) {
			logger.error(e.getMessage(), e);
			return false;
		} catch (IOException e) {
			throw e;
		}	
	}

	private void createSchemaIfNotExists(RestClient restClient) throws IOException {
		if (!exists(restClient)) {
			HttpEntity entity = new NStringEntity(IoUtil.loadFile("json/request_log_schema.json"), ContentType.APPLICATION_JSON);
			restClient.performRequest("PUT", INDEX_NAME, Collections.<String, String> emptyMap(), entity);
		}
	}

	private void writeLog(RestClient restClient, RequestLog requestLog) throws Exception{
		HttpEntity entity = new NStringEntity(JsonUtil.toJson(requestLog), ContentType.APPLICATION_JSON);
		restClient.performRequest("POST", INDEX_NAME+"/"+TYPE_NAME, Collections.<String, String> emptyMap(), entity);
	}
	
	@Override
	public void addRequestLog(RestClient restClient, RequestLog requestLog) {
		try {
			createSchemaIfNotExists(restClient);
			writeLog(restClient, requestLog);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Map> loadHistory(RestClient restClient, String username) {
		List<Map> logs = new ArrayList();
		String query = IoUtil.loadFile("json/request_log_query.json");
		query = query.replace("{0}", username);
		try(NStringEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON)) {
			Response indexResponse = restClient.performRequest("GET", INDEX_NAME+"/_search", Collections.<String, String> emptyMap(), entity);
			String response = IOUtils.toString(indexResponse.getEntity().getContent(), Charset.forName("UTF-8"));
			Map<String,Object> result = JsonUtil.readValue(response, Map.class);
			Map hits = (Map) result.get("hits");
			int total = (Integer)hits.get("total");
			if(total == 0){
				return logs;
			}
			List<Map> objs = (List<Map>) hits.get("hits");
			objs.stream().map(obj -> (Map)obj.get("_source")).forEach(s -> logs.add(s));
			
			return logs;
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return logs;
	}

}