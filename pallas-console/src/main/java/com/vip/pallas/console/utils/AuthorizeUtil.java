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

package com.vip.pallas.console.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.vip.pallas.bean.AuthorizeModel;
import com.vip.pallas.bean.PrivilegeResultModel;
import com.vip.pallas.service.PrivilegeService;
import com.vip.pallas.utils.PallasConsoleProperties;

@Component
public class AuthorizeUtil {
	private static Logger logger = LoggerFactory.getLogger(AuthorizeUtil.class);

	
	private static AuthorizeUtil authorizeUtil;
	
	@Autowired
	private PrivilegeService privilegeService;
	
	private static List<String> allPrivileges = Lists.newArrayList("cluster.all", "index.all", "version.all",
			"template.all", "plugin.all", "pallas-search.all", "authorization.all", "user.all");
	
	@PostConstruct
	public void init() {
	    authorizeUtil = this;
	    authorizeUtil.privilegeService = this.privilegeService;
	}
	
	private static boolean authorizeModulePrivilege(HttpServletRequest request, String module) {
		if (!isAuthorizationEnable()) {
			return true;
		}
		String username = SessionUtil.getLoginUser();
		if (username == null) {
			return false;
		}

		AuthorizeModel authorizeModel = new AuthorizeModel();
		authorizeModel.setUserId(username);
		authorizeModel.setAssetCode(PallasConsoleProperties.PALLAS_AUTHROIZE_APP + "." + module);
		authorizeModel.setActionTypeName("write");
		authorizeModel.setApplicationName(PallasConsoleProperties.PALLAS_AUTHROIZE_APP);
		try {
			return authorizeUtil.privilegeService.authorize(authorizeModel);
		} catch (Exception e) {//NOSONAR
			logger.error("authorize " + module + " error! request:" + request.getRequestURI(), e.getCause());
			return false;
		}
	}
	
	public static boolean isAuthorizationEnable() {
		if (PallasConsoleProperties.PALLAS_SECURITY_ENABLE && PallasConsoleProperties.PALLAS_AUTHORIZATION_ENABLE) {
			return true;
		}
		return false;
	}

	public static boolean authorizeClusterPrivilege(HttpServletRequest request, String clusterName) {
		boolean allPrivilege = authorizeModulePrivilege(request, "cluster.all");
		if (allPrivilege) {
			return true;
		}
		if (StringUtils.isNoneEmpty(clusterName)) {
			return authorizeModulePrivilege(request, "cluster." + clusterName);
		}
		return false;
	}

	public static boolean authorizeIndexPrivilege(HttpServletRequest request, Long indexId, String indexName) {
		boolean allPrivilege = authorizeModulePrivilege(request, "index.all");
		if (allPrivilege) {
			return true;
		}
		if (null != indexId && StringUtils.isNoneEmpty(indexName)) {
			return authorizeModulePrivilege(request, "index." + indexId + "-" + indexName);
		}
		return false;
	}
	
	public static boolean authorizeTemplatePrivilege(HttpServletRequest request, Long indexId, String indexName) {
		boolean allPrivilege = authorizeModulePrivilege(request, "template.all");
		if (allPrivilege) {
			return true;
		}
		if (null != indexId && StringUtils.isNoneEmpty(indexName)) {
			return authorizeModulePrivilege(request, "template." + indexId + "-" + indexName);
		}
		return false;
	}
	
	public static boolean authorizeTemplateApprovePrivilege(HttpServletRequest request) {
		return authorizeModulePrivilege(request, "template.all") ? true : authorizeModulePrivilege(request, "template.approve");
	}
	
	public static boolean authorizePluginApprovePrivilege(HttpServletRequest request) {
		return authorizeModulePrivilege(request, "plugin.all") ? true : authorizeModulePrivilege(request, "plugin.approve");
	}
	
	public static boolean authorizeIndexVersionPrivilege(HttpServletRequest request, Long indexId, String indexName) {
		boolean allPrivilege = authorizeModulePrivilege(request, "version.all");
		if (allPrivilege) {
			return true;
		}
		if (null != indexId && StringUtils.isNoneEmpty(indexName)) {
			return authorizeModulePrivilege(request, "version." + indexId + "-" + indexName);
		}
		return false;
	}
	
	public static boolean authorizeTokenPrivilege(HttpServletRequest request, String indexName) {
		return authorizeModulePrivilege(request, "authorization.all") ? true : false;
	}
	
	public static boolean authorizePSearchPrivilege(HttpServletRequest request, String indexName) {
		return authorizeModulePrivilege(request, "pallas-search.all") ? true : false;
	}

	public static List<String> loadPrivileges() { // NOSONAR
		if (!isAuthorizationEnable()) {
			return allPrivileges;
		}

		List<String> userAccountIds = new ArrayList<String>();
		String loginUser = SessionUtil.getLoginUser();
		if (loginUser == null) {
			return null; // NOSONAR
		}
		userAccountIds.add(loginUser);
		try {
            Map<String, List<PrivilegeResultModel>> privilegeMap = authorizeUtil.privilegeService
                    .getPrivilegeByUsers(userAccountIds);
			List<PrivilegeResultModel> models = privilegeMap.get(loginUser);
			if (models == null || models.isEmpty()) {
				return null; // NOSONAR
			}
			List<String> privileges = new ArrayList<String>();
			for (PrivilegeResultModel model : models) {
				privileges.add(model.getAssetCode().replace(PallasConsoleProperties.PALLAS_AUTHROIZE_APP + "." , ""));
			}
			return privileges;
		} catch (Exception e) { // NOSONAR
			return null; // NOSONAR
		}

	}
}