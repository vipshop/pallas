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

package com.vip.pallas.console.controller.api.server;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.SearchAuthorization;
import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.SearchAuthorizationService;
import com.vip.pallas.service.SearchServerService;
import com.vip.pallas.utils.IPUtils;
import com.vip.pallas.utils.ObjectMapTool;

@Validated
@RestController
@RequestMapping("/ss")
public class ServerApiController {
    private static Logger logger = LoggerFactory.getLogger(ServerApiController.class);

    @Autowired
    private SearchServerService searchServerService;

    @Autowired
    private SearchAuthorizationService searchAuthorizationService;

    @Autowired
    protected ClusterService clusterService;

    @RequestMapping(value = "/upsert.json", method = RequestMethod.POST)
	public void upsert(@RequestBody Map<String, Object> params) throws Exception {
		Object infoJson = params.get("info");
        String ipport = ObjectMapTool.getString(params, "ipport");
        String cluster = ObjectMapTool.getString(params, "cluster");
        String takeTrafficStr = ObjectMapTool.getString(params, "takeTraffic");
        SearchServer ss = new SearchServer();
        ss.setCluster(cluster);
        ss.setIpport(ipport);
        if (takeTrafficStr != null) {
            Boolean takeTraffic = Boolean.valueOf(takeTrafficStr);
            logger.info("server {} 's takeTraffic property is set to {}", ipport, takeTraffic);
            ss.setTakeTraffic(takeTraffic);
        }
        if (infoJson != null) {
			// info-parse-error should not affect the heart beat of ps.
			String info = infoJson.toString() + " to json error: ";
			try {
				info = JSON.toJSONString(infoJson);
			} catch (Exception e) {
				info += e.getMessage();
				logger.error(e.getMessage(), e);
			}
			ss.setInfo(info);
        }
        searchServerService.upsertByIpAndCluster(ss);
    }

    @RequestMapping("/query_pslist_and_domain.json")
    public Map<String, Object> queryPsListAndDomain(@RequestBody Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        String ip = ObjectMapTool.getString(params, "ip");
        String token = ObjectMapTool.getString(params, "token");
        // find esDomain by token
        SearchAuthorization authorization = searchAuthorizationService.findByToken(token);
        String domain;
        if (authorization != null && authorization.getAuthorizationItemList() != null
                && !authorization.getAuthorizationItemList().isEmpty()) {
            domain = authorization.getAuthorizationItemList().get(0).getName();
            resultMap.put("domain", domain);
        } else {
            throw new IllegalArgumentException("no domain matched by this token " + token);
        }
        if (!authorization.isEnabled()) { // access es directly.
            List<String> healthyPsList = genRealEsClusterDomainList(domain);
            resultMap.put("psList", healthyPsList);
            return resultMap;
        }
        // find pallasSearch ipList by ip(the same dc first)
        Cluster cluster = clusterService.findByName(domain);
        if (cluster == null) {
            throw new IllegalArgumentException(
                    "could not found cluster by domain: " + domain + " and token: " + token);
        }

        if (StringUtils.isEmpty(cluster.getAccessiblePs())) {
            throw new IllegalArgumentException(
                    "accessible ps not configured, domain: " + domain + " not found, ip: " + ip);
        }
        List<String> healthyPsList = findAccessibleHealthyPsList(cluster.getAccessiblePs(), ip);
        if (healthyPsList.isEmpty()) {
            logger.error("healthy servers allow to access this domain " + domain + " not found, ip:" + ip
                    + ", now return the es-domain which the client can make the direct accest to.");
            healthyPsList = genRealEsClusterDomainList(domain);
        }
        resultMap.put("psList", healthyPsList);
        return resultMap;
    }

    private List<String> genRealEsClusterDomainList(String domain) {
        List<String> healthyPsList = new ArrayList<>();
        Cluster cluster = clusterService.findByName(domain);
        if (cluster.isLogicalCluster()) {
            String realClusterIdsStr = cluster.getRealClusters();
            String[] realIds = realClusterIdsStr.split(",");
            for (String idStr : realIds) {
                Cluster c = clusterService.selectByPrimaryKey(Long.valueOf(idStr));
                healthyPsList.add(c.getClusterId() + ":9200");
            }
        } else {
            healthyPsList.add(domain + ":9200");
        }
        return healthyPsList;
    }

    private List<String> findAccessibleHealthyPsList(String accessiblePs, String ip) {
        List<String> ipList = new ArrayList<>();
        List<String> theSameDcNotFoundList = new ArrayList<>();
        String[] domainArray = accessiblePs.split(",");
        int targetPrefix = IPUtils.IPV42PrefixInteger(ip);
        for (String domain : domainArray) {
            List<SearchServer> ssList = searchServerService
                    .selectHealthyServersByCluster(SearchServerService.HEALTHY_UPLOAD_INTERVAL_TOLERANCE, domain);
            // if one ip of the cluster match the-same-dc rule, the rest shall be the same.
            if (CollectionUtils.isNotEmpty(ssList)
                    && targetPrefix == IPUtils.IPV42PrefixInteger(ssList.get(0).getIpport())) {
                ipList.addAll(ssList.stream().filter(SearchServer::getTakeTraffic).map(SearchServer::getIpport).collect(toList()));
            }
            // if the-same-dc-pslist not found, return all of them.
            theSameDcNotFoundList.addAll(ssList.stream().filter(SearchServer::getTakeTraffic).map(SearchServer::getIpport).collect(toList()));
        }
        return ipList.isEmpty() ? theSameDcNotFoundList : ipList;
    }
}