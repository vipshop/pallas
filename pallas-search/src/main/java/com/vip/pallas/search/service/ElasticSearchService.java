package com.vip.pallas.search.service;

import java.util.List;

public interface ElasticSearchService {
	
	List<String> getExcludeNodeList(String clusterAddress);

	List<String[]> getActualIndexs(String clusterHttpAddress);
	
	List<String[]> getIndexAndNodes(String clusterHttpAddress);
}
