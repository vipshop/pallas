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
import java.util.HashMap;
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
		Set<String> ipSet = SetUtil.newHashSet();
		pools = CollectionUtils.isEmpty(pools) ? SearchAuthorization.DEFAULT_POOLS : pools;
		List<String> domains = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(accessiblePs);
		// 初始化所有可能需要返回标签的结构，同时考虑某一search sever下无存活服务的可能
		Map<String, Set<String>> poolIpMap = MapUtil.newHashMap();
		for (Pool pool : pools) {
			if (!poolIpMap.containsKey(pool.genUniqueKey())) {
				poolIpMap.put(pool.genUniqueKey(), SetUtil.newHashSet());
			}
		}
		int targetPrefix = IPUtils.IPV42PrefixInteger(ip);
		List<SearchServer> sameDcSsList = ListUtil.newArrayList(), remoteDcSsList = ListUtil.newArrayList(),
				ipSsList = ListUtil.newArrayList();
		for (String domain : domains) {
			List<SearchServer> ssList = searchServerService
					.selectHealthyServersByCluster(SearchServerService.HEALTHY_UPLOAD_INTERVAL_TOLERANCE, domain);
			// if one ip of the cluster match the-same-dc rule, the rest shall be the same.
			ssList = ssList.stream().filter(SearchServer::getTakeTraffic).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(ssList)
					&& targetPrefix == IPUtils.IPV42PrefixInteger(ssList.get(0).getIpport())) {
				sameDcSsList.addAll(ssList);
			}
			// if the-same-dc-pslist not found, return all of them.
			remoteDcSsList.addAll(ssList);
		}

		ipSsList = sameDcSsList.isEmpty() ? remoteDcSsList : sameDcSsList;

		// 处理search server注册回来的所有的pool,并分组聚合search server，同时兼容处理可能的历史数据
		for (SearchServer server : ipSsList) {
			poolIpMap.get(Pool.DEFAULT_UNIQUE_KEY).add(server.getIpport());
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
		}

		poolIpMap.forEach((k, v) -> {
			if (!Pool.DEFAULT_UNIQUE_KEY.equals(k)) {
				ipSet.addAll(v);
			}
		});

		// 某pool下无任何存活search server时，返回default
		return ListUtil.newArrayList(CollectionUtils.isEmpty(ipSet) ? 
				poolIpMap.get(Pool.DEFAULT_UNIQUE_KEY) : ipSet);
    }
}