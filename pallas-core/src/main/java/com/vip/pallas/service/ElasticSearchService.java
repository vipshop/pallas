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

package com.vip.pallas.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.RestClient;

public interface ElasticSearchService {
	
	public String genMappingJsonByVersionId(Long versionId);
	
	public String createIndex(String indexName, Long versionId) throws IOException;
	
	public String deleteIndex(String indexName, Long versionId) throws IOException;

	public String deleteIndex(String clusterAddress, String fullIndexName);

	public String createAliasIndex(String indexName, Long versionId) throws Exception;	
	
	public String deleteAliasIndex(Long indexId, String indexName, Long versionId, Long clusterId) throws Exception;

	String transferAliasIndex(Long indexId, String indexName, Long targetVersionId, Long clusterId) throws Exception;
	
	public boolean isExistIndex(String indexName, Long versionId);
	
	public Long getDataCount(String indexName, Long versionId);
	
	public String getIndexInfo(String indexName, Long versionId);

	boolean excludeOneNode(String host, String nodeIp);
	
	boolean includeOneNode(String host, String nodeIp);

	List<String> getExcludeNodeList(String clusterAddress);

	List<String> getAvalableNodeIps(String clusterAddress) throws IOException;

	/**
	 * 根据索引名（别名）找出全部索引分布集群及分片节点
	 * @param aliasIndexNames
	 * @return
	 */
	Map<String, List<String>> getShardNodesByAlias(String aliasIndexNames);

	String getClusterStatus(String clusterName) throws Exception;

	Map<String, String> getMainClusterSettings(String clusterName) throws Exception;

	List<String[]> getNodes(String clusterName) throws Exception;

	List<String[]> getShards(String clusterName) throws Exception;

	List<String[]> getActualIndexs(String clusterHttpAddress);

	List<String[]> getNormalIndexs(String clusterHttpAddress);

	List<String[]> getIndexAndNodes(String clusterHttpAddress);

	String getHttpAddressByClusterName(String clusterName);

	RestClient getRestClientByClusterName(String clusterName);

	String setIndexBlockWrite(RestClient clusterRestClient, String actualIndexName, boolean isBlockWrite) throws IOException;

	String setClusterAllocation(RestClient restClient, String enable) throws IOException;

	String setClusterRebalance(RestClient restClient, String enable) throws IOException;

	String setClusterDelayedTimeout(RestClient restClient, String timeout) throws IOException;

	String setClusterFlushSynced(RestClient restClient) throws IOException;

	String executeDeleteByQueryDsl(Long versionId, String indexName, String dsl, int scrollSize);

	String executeSearchByDsl(Long versionId, String indexName, String dsl);

	String cancelDeleteByQueryTask(Long versionId, String indexName, String lastMsg);
}