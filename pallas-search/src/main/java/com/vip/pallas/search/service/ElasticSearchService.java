package com.vip.pallas.search.service;

import java.util.List;
import java.util.Map;

import com.vip.pallas.search.model.ShardGroup;

public interface ElasticSearchService {
	
	List<String> getExcludeNodeList(String clusterAddress);

	List<String[]> getActualIndexs(String clusterHttpAddress);
	
	List<String[]> getIndexAndNodes(String clusterHttpAddress);

	List<ShardGroup> genDynamicGroup(String clusterHttpAddress, String indexName,
			Map<String, String> nodesInfo);

	Map<String, String> getNodesInfo(String clusterHttpAddress);
}
