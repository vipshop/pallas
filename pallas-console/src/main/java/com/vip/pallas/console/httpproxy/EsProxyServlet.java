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

package com.vip.pallas.console.httpproxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.console.utils.ThrottleUtil;
import com.vip.pallas.console.vo.base.ErrorResponse;
import com.vip.pallas.mybatis.entity.RequestLog;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.RequestLogService;
import com.vip.pallas.service.impl.RequestLogServiceImpl;
import com.vip.pallas.utils.DateUtil;
import com.vip.pallas.utils.JsonUtil;
import com.vip.pallas.utils.PallasConsoleProperties;

public class EsProxyServlet extends HttpServlet {
	private static Logger logger = LoggerFactory.getLogger(EsProxyServlet.class);
	private ElasticSearchService elasticSearchService;
	private RequestLogService requestLogService;
	private static final long serialVersionUID = 2528291424860077354L;


	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String request = req.getHeader("es-request");
		if (!Boolean.valueOf(request)) {
			return;
		}

		String clusterId = req.getHeader("es-cluster");
		if (!AuthorizeUtil.authorizeClusterPrivilege(req, clusterId)) {
			writeReponseBody(res,new ErrorResponse(HttpStatus.SC_UNAUTHORIZED, PallasConsoleProperties.PALLAS_LOGIN_URL));
			return;
		}
		

		if (ThrottleUtil.esClusterInc(clusterId) > PallasConsoleProperties.PROXY_THROTTLE) {
			ThrottleUtil.esClusterDesc(clusterId);
			writeReponseBody(res, new ErrorResponse(305, "Too many requests"));
			return;
		}
		try {
			String host = req.getHeader("es-host");
			String method = req.getMethod();
			String path = req.getRequestURI();
			String queryParams = req.getQueryString();
			path = path.replaceFirst("/pallas", "");
			path = path.replaceFirst("/esproxy", "");
			if (StringUtils.isNotBlank(queryParams)) {
				if (path.indexOf("?") < 0) {
					path += "?" + queryParams;
				}
			}

			String requestData = "";
			if (!method.equalsIgnoreCase("get")) {
				requestData = IOUtils.toString(req.getInputStream());
				if (StringUtils.isBlank(requestData)) {
					Map<String, String[]> parameterMap = req.getParameterMap();
					for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
						requestData += entry.getKey();
						if (entry.getValue() != null && StringUtils.isNotBlank(entry.getValue()[0])) {
							requestData += "=" + entry.getValue()[0];
						}
					}
				}
			}
		
			RequestLog requestLog = new RequestLog();
			requestLog.setBody(requestData);
			requestLog.setCreated_at(DateUtil.getCurrentDateTime("yyyy-MM-dd HH:mm:ss"));
			requestLog.setMethod(method);
			requestLog.setPath(path);
			requestLog.setUsername(SessionUtil.getLoginUser(req));

			RestClient restClient = elasticSearchService.getRestClientByClusterName(clusterId);
			requestLogService.addRequestLog(restClient, requestLog);
			
			if (requestData != null) {
				requestData = requestData.replace("\r\n", "");
				requestData = requestData.replace("\n", "");
			}
			AuditLogUtil.log("host:{0} path:{1} request: {2}", host, path, requestData);

			HttpEntity entity = new NStringEntity(requestData, ContentType.APPLICATION_JSON);
			Response performRequest = restClient.performRequest(method, path, Collections.<String, String> emptyMap(),
					entity);
			String result = EntityUtils.toString(performRequest.getEntity(), Charset.forName("UTF-8"));

			res.setContentType("application/json; charset=UTF-8");

			res.getWriter().write(result);
			res.getWriter().flush();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			res.getWriter().write(e.getMessage());
			res.getWriter().flush();
		} finally {
			ThrottleUtil.esClusterDesc(clusterId);
		}
	}

	private void writeReponseBody(HttpServletResponse response, ErrorResponse errorResponse){
		String responsebody = null;
		try {
			responsebody = JsonUtil.toJson(new ErrorResponse(errorResponse.getStatus(), errorResponse.getMessage()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			responsebody = "{\"status\":500,\"message\":\"" + e.getMessage() +"\"}";
		}
		try (PrintWriter writer = response.getWriter();){
			writer.print(responsebody);
			writer.flush();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override
	public void init() throws ServletException {
		requestLogService = new RequestLogServiceImpl();
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		this.elasticSearchService = (ElasticSearchService) wac.getBean("elasticSearchServiceImpl");
	}
}