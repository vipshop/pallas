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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.utils.MultiReadHttpServletRequest;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.console.utils.ThrottleUtil;
import com.vip.pallas.console.vo.base.ErrorResponse;
import com.vip.pallas.utils.JsonUtil;
import com.vip.pallas.utils.PallasConsoleProperties;

public class CerebroServlet extends ProxyServlet {
	private static Logger logger = LoggerFactory.getLogger(CerebroServlet.class);
	
	private static final long serialVersionUID = 4725686031976614625L;

	private static final String[] excludeUrls = new String[] { "/cerebro/navbar", "/cerebro/overview",
			"/cerebro/cluster_changes", "/cerebro/commons/indices", "/cerebro/nodes", "/cerebro/rest",
			"/cerebro/rest/history", "/cerebro/cluster_settings", "/cerebro/aliases/get_aliases",
			"/cerebro/commons/indices", "/cerebro/commons/nodes", "/cerebro/commons/get_index_settings",
			"/cerebro/commons/get_index_mapping", "/cerebro/commons/get_node_stats", "/cerebro/templates",
			"/cerebro/index_settings", "/cerebro/repositories", "/cerebro/snapshots" };

	private Map<String, String> params;

	public String getConfigParam(String key) {
		return params.get(key);
	}

	@Override
	public void init() throws ServletException {
		params = new HashMap<String, String>();
		params.put(P_TARGET_URI, "http://127.0.0.1:"+PallasConsoleProperties.CEREBRO_PORT);
		super.init();
	}

	@Override
	public void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
			throws ServletException, IOException { // NOSONAR
		
		if (PallasConsoleProperties.PALLAS_SECURITY_ENABLE && !SessionUtil.isAuthorization(servletRequest)) {
			writeReponseBody(servletResponse, new ErrorResponse(HttpStatus.SC_UNAUTHORIZED, PallasConsoleProperties.PALLAS_LOGIN_URL));
			return;
		}
		
		String clusterId = servletRequest.getHeader("es-cluster");
		if (!AuthorizeUtil.authorizeClusterPrivilege(servletRequest, clusterId)) {
			writeReponseBody(servletResponse, new ErrorResponse(304, "You are not authroized."));
			return;
		}
		
		if(ThrottleUtil.esClusterInc(clusterId) > PallasConsoleProperties.PROXY_THROTTLE){
			ThrottleUtil.esClusterDesc(clusterId);
			writeReponseBody(servletResponse, new ErrorResponse(305, "Too many requests."));
			return;
		}
		try{	
			String uri = servletRequest.getRequestURI();
			
			if (Stream.of(excludeUrls).anyMatch(excludeUrl -> uri.equalsIgnoreCase(excludeUrl))) {
				super.service(servletRequest, servletResponse);
				return;
			}
			MultiReadHttpServletRequest request = new MultiReadHttpServletRequest(servletRequest);
			AuditLogUtil.log("request: {0}", request.toString());
			super.service(request, servletResponse);
		}catch(Exception e){
			writeReponseBody(servletResponse, new ErrorResponse(306, "Error request."));
		}finally{
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
}