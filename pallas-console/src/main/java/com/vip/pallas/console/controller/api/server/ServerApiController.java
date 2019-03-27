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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.google.common.base.Splitter;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.SearchAuthorization;
import com.vip.pallas.mybatis.entity.SearchAuthorization.AuthorizationItem;
import com.vip.pallas.mybatis.entity.SearchAuthorization.Pool;
import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.SearchAuthorizationService;
import com.vip.pallas.service.SearchServerService;
import com.vip.pallas.utils.IPUtils;
import com.vip.pallas.utils.ObjectMapTool;
import com.vip.vjtools.vjkit.collection.ListUtil;
import com.vip.vjtools.vjkit.collection.MapUtil;
import com.vip.vjtools.vjkit.collection.SetUtil;

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
        Set<String> poolSet = ObjectMapTool.getStringSet(params, "pools");
        SearchServer ss = new SearchServer();
        ss.setCluster(cluster);
        ss.setIpport(ipport);
        if (takeTrafficStr != null) {
            Boolean takeTraffic = Boolean.valueOf(takeTrafficStr);
			// logger.info("server {} 's takeTraffic property is set to {}", ipport, takeTraffic);
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

		ss.setPools(Pool.toPoolsConetent(poolSet));
		searchServerService.upsertByIpAndCluster(ss);
	}

    @RequestMapping("/query_pslist_and_domain.json")
    public Map<String, Object> queryPsListAndDomain(@RequestBody Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<>();
        String ip = ObjectMapTool.getString(params, "ip");
        String token = ObjectMapTool.getString(params, "token");
        // find esDomain by token
        SearchAuthorization authorization = searchAuthorizationService.findByToken(token);
        AuthorizationItem authorizationItem = null;
        if (authorization != null && authorization.getAuthorizationItemList() != null
                && !authorization.getAuthorizationItemList().isEmpty()) {
        	authorizationItem = authorization.getAuthorizationItemList().get(0);
            resultMap.put("domain", authorizationItem.getName());
        } else {
            throw new IllegalArgumentException("no domain matched by this token " + token);
        }
        if (!authorization.isEnabled()) { // access es directly.
            List<String> healthyPsList = genRealEsClusterDomainList(authorizationItem.getName());
            resultMap.put("psList", healthyPsList);
            return resultMap;
        }
        // find pallasSearch ipList by ip(the same dc first)
        Cluster cluster = clusterService.findByName(authorizationItem.getName());
        if (cluster == null) {
            throw new IllegalArgumentException(
                    "could not found cluster by domain: " + authorizationItem.getName() + " and token: " + token);
        }

        if (StringUtils.isEmpty(cluster.getAccessiblePs())) {
			throw new IllegalArgumentException(
					"accessible ps not configured, domain: " + authorizationItem.getName() + " not found, ip: " + ip);
        }
        List<String> healthyPsList = findAccessibleHealthyPsList(cluster.getAccessiblePs(), authorizationItem.getPools(), ip);
        if (healthyPsList.isEmpty()) {
            logger.error("healthy servers allow to access this domain :" + authorizationItem.getName() + " not found, "
                    + "ip:" + ip  +" now return the es-domain which the client can make the direct accest to.");
            healthyPsList = genRealEsClusterDomainList(authorizationItem.getName());
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

	private List<String> findAccessibleHealthyPsList(String accessiblePs, Set<Pool> pools, String ip) {
		int targetPrefix = IPUtils.IPV42PrefixInteger(ip);
		pools = CollectionUtils.isEmpty(pools) ? Collections.emptySet() : pools;
		List<String> domains = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(accessiblePs);
		Set<String> globalSameDcIpSet = SetUtil.newHashSet(), globalIpSet = SetUtil.newHashSet();
		// initialize the pool map to store the ipport for every pool.
		Map<String, Set<String>> poolIpMap = pools.stream()
				.collect(Collectors.toMap(Pool::genUniqueKey, p -> SetUtil.newHashSet(), (key1, key2) -> key1));
		
		for (String domain : domains) {
			List<SearchServer> ssList = searchServerService
					.selectHealthyServersByCluster(SearchServerService.HEALTHY_UPLOAD_INTERVAL_TOLERANCE, domain);

			// if one ip of the domain match the-same-dc rule, the rest shall be the same.
			if (CollectionUtils.isNotEmpty(ssList)
					&& targetPrefix == IPUtils.IPV42PrefixInteger(ssList.get(0).getIpport())) {
				globalSameDcIpSet.addAll(ssList.stream().filter(SearchServer::getTakeTraffic)
						.map(SearchServer::getIpport).collect(Collectors.toSet()));
			}
			
			ssList.stream().filter(SearchServer::getTakeTraffic).forEach(server -> {
				globalIpSet.add(server.getIpport());
				Set<String> poolSet = Pool.DEFAULT_POOL_ARR;
				try {
					poolSet = Pool.fromPoolsContent(server.getPools());
				} catch (Exception e) {
					logger.error("Pools illegal", e);
				}
				for (String pool : poolSet) {
					String poolKey = Pool.getUniqueKey(pool, server.getCluster());
					if (poolIpMap.containsKey(poolKey)) {
						poolIpMap.get(poolKey).add(server.getIpport());
					}
				}
			});
		}
		
		Set<String> poolSameDcIpSet = SetUtil.newHashSet(), poolIpSet = SetUtil.newHashSet();
		poolIpMap.values().forEach(v -> {
			poolIpSet.addAll(v);
			Iterator<String> i = v.iterator();
			// if one ip of the pool match the-same-dc rule, the rest shall be the same.
			if (i.hasNext() && targetPrefix == IPUtils.IPV42PrefixInteger(i.next())) {
				poolSameDcIpSet.addAll(v);
			}
		});
		
		return ListUtil.newArrayList(
				CollectionUtils.isNotEmpty(poolSameDcIpSet) ? poolSameDcIpSet
					: CollectionUtils.isNotEmpty(poolIpSet) ? poolIpSet
						: CollectionUtils.isNotEmpty(globalSameDcIpSet) ? globalSameDcIpSet : globalIpSet);
	}
}