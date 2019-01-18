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

package com.vip.pallas.console.controller.cerebro;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.codehaus.jackson.JsonNode;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.mybatis.entity.RequestLog;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.IndexRoutingService;
import com.vip.pallas.service.RequestLogService;
import com.vip.pallas.utils.DateUtil;
import com.vip.pallas.utils.ElasticRestClient;
import com.vip.pallas.utils.ObjectMapTool;

@RestController
public class CerebroController {

	@Autowired
	private ElasticSearchService elasticSearchService;
	
	@Autowired
	private IndexRoutingService indexRoutingService;
	@Autowired
	private RequestLogService requestLogService;

	@RequestMapping(value = "/cerebro_proxy/rest/history.json")
	public Map<String, Object> history(String host, HttpServletRequest request) { // NOSONAR
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if(host == null){
			return resultMap;
		}

		RestClient restClient = ElasticRestClient.build(host);
		List<Map> logs = requestLogService.loadHistory(restClient, SessionUtil.getLoginUser(request));
		if (logs != null) {
			resultMap.put("body", logs);
		}
		return resultMap;
	}
	@RequestMapping(value = "/cerebro_proxy/rest/request.json")
	public Map<String, Object> request(@RequestBody Map<String, Object> params, HttpServletRequest request) throws Exception { // NOSONAR
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String host = ObjectMapTool.getString(params, "host");
		String method = ObjectMapTool.getString(params, "method");
		String path = ObjectMapTool.getString(params, "path");

		JsonNode node = ObjectMapTool.getObject(params, "path", JsonNode.class);

		HttpEntity entity = new NStringEntity(node.toString(), ContentType.APPLICATION_JSON);

		RestClient restClient = ElasticRestClient.build(host);
		AuditLogUtil.log("request: host={0}, path={1}, body={2} ", host, path, node.toString());

		RequestLog requestLog = new RequestLog();
		requestLog.setBody(node.toString());
		requestLog.setCreated_at(DateUtil.getCurrentDateTime("yyyy-MM-dd HH:mm:ss"));
		requestLog.setMethod(method);
		requestLog.setPath(path);
		requestLog.setUsername(SessionUtil.getLoginUser(request));
		requestLogService.addRequestLog(restClient, requestLog);

		Response indexResponse = null;
		try {
			indexResponse = restClient.performRequest(method, path, Collections.<String, String> emptyMap(), entity);
			String response = IOUtils.toString(indexResponse.getEntity().getContent());
			resultMap.put("body", response);
		} catch (ResponseException e) { // NOSONAR
			resultMap.put("body", e.getMessage());
		} catch (IOException e) { // NOSONAR
			resultMap.put("body", e.getMessage());
		}

		return resultMap;
	}
	@RequestMapping(value = "/exclude/node.json")
	public void excludeNode(@RequestBody Map<String, Object> params) { // NOSONAR
		String host = ObjectMapTool.getString(params, "host");
		String nodeIp = ObjectMapTool.getString(params, "nodeIp");
		Boolean exclude = ObjectMapTool.getBoolean(params, "exclude");
		String cluster = ObjectMapTool.getString(params, "cluster");
		if (StringUtils.isNoneBlank(host) && StringUtils.isNoneBlank(nodeIp) && StringUtils.isNoneBlank(cluster)) {
			if (exclude) {
				elasticSearchService.excludeOneNode(host, nodeIp);
				indexRoutingService.updateNodeState(cluster, nodeIp, 1);
				AuditLogUtil.log("cluster {0} excludes node: {1}.", host, nodeIp);
			} else {
				elasticSearchService.includeOneNode(host, nodeIp);
				indexRoutingService.updateNodeState(cluster, nodeIp, 0);
				AuditLogUtil.log("cluster {0} includes node: {1}.", host, nodeIp);
			}
		}
	}
}