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

package com.vip.pallas.search.service.impl;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vip.pallas.search.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.cache.RoutingCache;
import com.vip.pallas.search.model.ShardGroup;
import com.vip.pallas.search.service.ElasticSearchService;
import com.vip.pallas.search.utils.ElasticRestClient;
import com.vip.pallas.search.utils.ElasticSearchStub;
import com.vip.pallas.utils.DivideShards;

public class ElasticSearchServiceImpl implements ElasticSearchService {
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);

	@Override
	public List<String> getExcludeNodeList(String clusterAddress) {
		try {
			RestClient restClient = ElasticRestClient.build(clusterAddress);
			String responseStr = getClusterSettings(restClient);

			if (responseStr.indexOf("cluster.routing.allocation.exclude._ip") >= 0) {
				String ipList = StringUtils.substringBetween(responseStr,"\"cluster.routing.allocation.exclude._ip\":\"","\"");
				if (StringUtils.isNotEmpty(ipList)) {
					return Arrays.asList(ipList.split(","));
				}
			}
		} catch (Exception e) {
			LogUtils.error(logger, SearchLogEvent.ROUTING_EVENT, "getExcludeNodeList by {} error: " + e, clusterAddress);
		}
		return emptyList();
	}

	//alias -> actual
	@Override
	public List<String[]> getActualIndexs(String clusterHttpAddress) {
		return ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/aliases/", (String line,List<String[]> list) -> {
			String[] infos = line.split("\\s+");
			if(infos.length > 2){
				list.add(new String[]{infos[0], infos[1]});
			}
		});
	}
	
	//aliasIndex -> shardNode
	@Override
	public List<String[]> getIndexAndNodes(String clusterHttpAddress) {
		return ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/shards/", (String line,List<String[]> list) ->{
			String[] infos = line.split("\\s+");
			if(infos.length > 6 && ("STARTED".equals(infos[3]) || "RELOCATING".equals(infos[3]))){
				list.add(new String[]{infos[0], infos[6]});
			}
		});
	}
	
	private String getClusterSettings(RestClient restClient) throws IOException {
		Response response = restClient.performRequest("GET", "/_cluster/settings?flat_settings=true",
		        Collections.singletonMap("pretty", "false"));
		return IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF-8"));
	}

	// data return from es e.g. 192.168.171.187 aGdouwo0SpOwk3OglbsyTa
	@Override
	public Map<String, String> getNodesInfo(String clusterHttpAddress) {
		List<String[]> result = ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/nodes?h=ip,id&full_id=true",
				(String line, List<String[]> list) -> {
					String[] infos = line.split("\\s+");
					if (infos.length == 2) {
						list.add(new String[] { infos[0], infos[1] });
					}
				});
		Map<String, String> ipIdMap = new HashMap<>();
		for (String[] ipAndId : result) {
			ipIdMap.put(ipAndId[0], ipAndId[1]);
		}
		return ipIdMap;
	}
	
	@Override
	public List<ShardGroup> genDynamicGroup(String clusterHttpAddress, String indexName,
			Map<String, String> nodesInfo) {
		try {
			List<String[]> result = ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/shards/" + indexName + "?h=shard,ip",
					(String line, List<String[]> list) -> {
						String[] infos = line.split("\\s+");
						if (infos.length == 2) {
							list.add(new String[] { infos[0], infos[1] });
						}
					});
			HashSet<String> nodes = new HashSet<>();
			Map<Integer, List<String>> shardDistributionMap = new HashMap<>();
			for (String[] shardAndIp : result) {
				shardDistributionMap.computeIfAbsent(Integer.valueOf(shardAndIp[0]), k -> {
					return new ArrayList<>();
				}).add(shardAndIp[1]);
				nodes.add(shardAndIp[1]);
			}
			if (!shardDistributionMap.isEmpty() && !nodesInfo.isEmpty()) {
				List<HashSet<String>> groupList = DivideShards.divideShards2Group(shardDistributionMap,
						shardDistributionMap.get(0).size(), nodes);
				List<ShardGroup> shardGroupList = new ArrayList<>();
				String port = RoutingCache.extractPortFromAddress(clusterHttpAddress);
				for (HashSet<String> group : groupList) {
					String preferNodes = group.stream().map(ip -> nodesInfo.get(ip)).collect(Collectors.joining(","));
					List<String> ipAndPortList = group.stream().map(ip -> ip + ":" + port).collect(Collectors.toList());
					shardGroupList.add(new ShardGroup(preferNodes, ipAndPortList, indexName));
				}

				return shardGroupList;

			}
		} catch (Exception e) {
			LogUtils.error(logger, SearchLogEvent.ROUTING_EVENT, e.getMessage(), e);
		}
		return emptyList();
	}
	
}


