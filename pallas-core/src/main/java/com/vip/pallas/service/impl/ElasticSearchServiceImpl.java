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

package com.vip.pallas.service.impl;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.vip.pallas.bean.ClusterSettings;
import com.vip.pallas.bean.EsAliases;
import com.vip.pallas.bean.EsMappings;
import com.vip.pallas.bean.EsMappings.Item;
import com.vip.pallas.bean.EsMappings.Mappings;
import com.vip.pallas.bean.EsMappings.Propertie;
import com.vip.pallas.bean.IndexSettings;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.IndexVersion;
import com.vip.pallas.mybatis.entity.Mapping;
import com.vip.pallas.mybatis.repository.ClusterRepository;
import com.vip.pallas.mybatis.repository.IndexRepository;
import com.vip.pallas.mybatis.repository.IndexVersionRepository;
import com.vip.pallas.mybatis.repository.MappingRepository;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.utils.ElasticRestClient;
import com.vip.pallas.utils.ElasticSearchStub;
import com.vip.pallas.utils.JsonUtil;

@Service
@Transactional(rollbackFor=Exception.class)
public class ElasticSearchServiceImpl implements ElasticSearchService {
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);
	
	@Resource
	private MappingRepository mappingRepository;
	
	@Resource
	ClusterRepository clusterRepository;
	
	@Resource
	private IndexVersionRepository indexVersionRepository;

	@Resource
	private IndexRepository indexRepository;

	@Override
	public String genMappingJsonByVersionId(Long versionId) {
		List<Mapping> mappingList = mappingRepository.selectByVersionId(versionId);
		IndexVersion indexVersion = indexVersionRepository.selectByPrimaryKey(versionId);
		if(mappingList == null){
			return null;
		}
		
		EsMappings esMappings = new EsMappings();
		Mappings mappings = new Mappings();
		Item item = new Item();
		Map<String, Propertie> propertieMap = new HashMap<String, Propertie>();
		
		esMappings.setMappings(mappings);
		
		Map<String, Object> settings = new HashMap<>();
		settings.put("number_of_shards", indexVersion.getNumOfShards());
		settings.put("number_of_replicas", indexVersion.getNumOfReplication());
		settings.put("refresh_interval", indexVersion.getRefreshInterval() + "s");
		settings.put("translog.durability", "async");

		//设置slowlog
		settings.put("indexing.slowlog.level", "info");
		settings.put("indexing.slowlog.threshold.index.info", indexVersion.getIndexSlowThreshold() != -1 ? indexVersion.getIndexSlowThreshold() + "ms" : "-1");
		settings.put("indexing.slowlog.source", "0");

		settings.put("search.slowlog.threshold.fetch.info", indexVersion.getFetchSlowThreshold() != -1 ? indexVersion.getFetchSlowThreshold() + "ms" : "-1");
		settings.put("search.slowlog.threshold.query.info", indexVersion.getQuerySlowThreshold() != -1 ? indexVersion.getQuerySlowThreshold() + "ms" : "-1");

		//设置allocation nodes
		String nodes = indexVersion.getAllocationNodes();
		if (!StringUtils.isEmpty(nodes)) {
			settings.put("index.routing.allocation.include._name", nodes);
		}
		esMappings.setSettings(settings);
		
		item.setIncludeInAll(false);
		item.setDynamic(false);
		item.setProperties(propertieMap);
		
		mappings.setItem(item);
		
		Map<Long, List<Mapping>> mappingMap = new HashMap<>();
		List<Mapping> firstLayerList = new ArrayList<>();

		constructMappings(mappingList, firstLayerList, mappingMap);
		
		addSourceField(firstLayerList);
		addIdField(firstLayerList);
		
		boolean hasNgramAnalyzer = false;
		boolean hasNormalizedKeyword = false;

		for (Mapping mapping : firstLayerList) {
			List<Mapping> nestedMappings = mappingMap.get(mapping.getId());
			Propertie prop = new Propertie();
			
			if(nestedMappings == null){
				prop.setType(mapping.getESRealFieldType());
				prop.setDocValues(mapping.getDocValue());
				prop.setIndex(mapping.getSearch());
				if (mapping.isNGramText()) {
					hasNgramAnalyzer = true;
					prop.setAnalyzer("edge_ngram_analyzer");
				}
				if (mapping.isNormalizedKeyword()) {
					hasNormalizedKeyword = true;
					prop.setNormalizer("uppercase_normalizer");
				}
			}
			
			if(prop.isDateType()){
				prop.setFormat("yyyy-MM-dd'T'HH:mm:ssZ||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm:ss'.0'||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd||epoch_millis");
			}
			propertieMap.put(mapping.getFieldName(), prop);
			
			if(nestedMappings != null){
				Map<String, Propertie> nestedPropertieMap = new HashMap<>();
				if (mapping.getDynamic()) {
					prop.setDynamic(Boolean.TRUE);
				}
				prop.setType("nested");
				prop.setDocValues(null);
				prop.setIndex(null);

				prop.setProperties(nestedPropertieMap);
				
				for (Mapping nestedMapping : nestedMappings) {
					Propertie nestedProp = new Propertie();
					nestedProp.setType(nestedMapping.getESRealFieldType());
					nestedProp.setDocValues(nestedMapping.getDocValue());
					nestedProp.setIndex(nestedMapping.getSearch());
					if(nestedProp.isDateType()){
						nestedProp.setFormat("yyyy-MM-dd'T'HH:mm:ssZ||yyyy-MM-dd HH:mm:ss||yyyy-MM-dd HH:mm:ss'.0'||yyyy-MM-dd HH:mm:ss.SSS||yyyy-MM-dd||epoch_millis");
					}
					if (nestedMapping.isNGramText()) {
						hasNgramAnalyzer = true;
					}
					if (nestedMapping.isNormalizedKeyword()) {
						hasNormalizedKeyword = true;
					}
					nestedPropertieMap.put(nestedMapping.getFieldName(), nestedProp);
				}
			}
		}

		//#538 pallas-console版本管理，索引字段支持es自有分词插件及ik插件的选择
		if (hasNgramAnalyzer || hasNormalizedKeyword) {
			addAnalysisSettings(settings, hasNgramAnalyzer, hasNormalizedKeyword);
		}
		String s =  new Gson().toJson(esMappings);
		return s;
	}

	private void addAnalysisSettings(Map<String, Object> settings, boolean hasNgramAnalyzer, boolean hasNormalizedKeyword) {
		Map<String, Object> analysisBody = new HashMap<>();

		if (hasNormalizedKeyword) {
			Map<String, Object> uppercaseNormalizerBody = new HashMap<>();
			uppercaseNormalizerBody.put("filter", new String[]{"uppercase"});
			uppercaseNormalizerBody.put("type", "custom");

			Map<String, Object> normalizerBody = new HashMap<>();
			normalizerBody.put("uppercase_normalizer", uppercaseNormalizerBody);

			analysisBody.put("normalizer", normalizerBody);
		}

		if (hasNgramAnalyzer) {
			Map<String, Object> analyzerBody = new HashMap<>();

			Map<String, Object> edgeNgramAnalyzerBody = new HashMap<>();
			edgeNgramAnalyzerBody.put("filter", new String[]{"uppercase"});
			edgeNgramAnalyzerBody.put("tokenizer", "ngram_tokenizer");

			analyzerBody.put("edge_ngram_analyzer", edgeNgramAnalyzerBody);
			analysisBody.put("analyzer", analyzerBody);

			Map<String, Object> tokenizerBody = new HashMap<>();
			Map<String, Object> ngramTokenizerBody = new HashMap<>();
			ngramTokenizerBody.put("token_char", new String[]{"letter", "digit"});
			ngramTokenizerBody.put("min_gram", "1");
			ngramTokenizerBody.put("max_gram", "1");
			ngramTokenizerBody.put("type", "nGram");

			tokenizerBody.put("ngram_tokenizer", ngramTokenizerBody);

			analysisBody.put("tokenizer", tokenizerBody);
		}

		settings.put("analysis", analysisBody);
	}

	public void addSourceField(List<Mapping> firstLayerList) {
		Mapping sourceMapping = new Mapping();
		sourceMapping.setSearch(Boolean.TRUE);
		sourceMapping.setDocValue(Boolean.TRUE);
		sourceMapping.setFieldName("_source_");
		sourceMapping.setFieldType("keyword");
		
		firstLayerList.add(sourceMapping);
	}

	public void addIdField(List<Mapping> firstLayerList) {
		Mapping sourceMapping = new Mapping();
		sourceMapping.setSearch(Boolean.TRUE);
		sourceMapping.setDocValue(Boolean.TRUE);
		sourceMapping.setFieldName("id");
		sourceMapping.setFieldType("long");
		
		firstLayerList.add(sourceMapping);
	}

	public static void constructMappings(List<Mapping> mappingList, List<Mapping> firstLayerList, Map<Long, List<Mapping>> mappingMap) {
		for (Mapping mapping : mappingList) {
			if(mapping.getParentId() == null){
				firstLayerList.add(mapping);
			}else{
				List<Mapping> nestedMappings = mappingMap.get(mapping.getParentId());
				if(nestedMappings == null){
					nestedMappings = new ArrayList<Mapping>();
					mappingMap.put(mapping.getParentId(), nestedMappings);
				}
				nestedMappings.add(mapping);
			}
		}
	}

	@Override
	public String createIndex(String indexName, Long versionId) throws IOException {
		if(this.isExistIndex(indexName, versionId)){
			this.deleteIndex(indexName, versionId);
		}
		
        try {
            NStringEntity entity = new NStringEntity(genMappingJsonByVersionId(versionId), ContentType.APPLICATION_JSON);
            return IOUtils.toString(ElasticRestClient.build(clusterRepository.selectByVersionId(versionId).getHttpAddress()).
            		performRequest("PUT", "/" + indexName + "_" + versionId, Collections.emptyMap(), entity).getEntity().getContent());
        } catch (IOException e) {
        	logger.error(e.getClass() + " " + e.getMessage(), e);
            throw e;
        }
	}

	@Override
	public String deleteIndex(String indexName, Long versionId) {
		try {
            NStringEntity entity = new NStringEntity(genMappingJsonByVersionId(versionId), ContentType.APPLICATION_JSON);
            return IOUtils.toString(ElasticRestClient.build(clusterRepository.selectByVersionId(versionId).getHttpAddress()).
            		performRequest("DELETE", "/" + indexName + "_" + versionId, Collections.emptyMap(), entity).getEntity().getContent());
        } catch (IOException e) {
        	logger.error(e.getClass() + " " + e.getMessage(), e);
        }
		
		return null;
	}

	@Override
	public String deleteIndex(String clusterAddress, String fullIndexName) {
		try {
			return IOUtils.toString(ElasticRestClient.build(clusterAddress).performRequest("DELETE", fullIndexName,
					Collections.emptyMap()).getEntity().getContent());
		} catch (IOException e) {
			logger.error(e.getClass() + " " + e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String createAliasIndex(String indexName, Long versionId)
			throws Exception {
		try {
            NStringEntity entity = new NStringEntity(genCreateAliasJson(indexName, versionId), ContentType.APPLICATION_JSON);
            return IOUtils.toString(ElasticRestClient.build(clusterRepository.selectByVersionId(versionId).getHttpAddress()).
            		performRequest("POST", "/_aliases", Collections.emptyMap(), entity).getEntity().getContent());
        } catch (IOException e) {
        	logger.error(e.getClass() + " " + e.getMessage(), e);
            throw e;
        }
	}

	@Override
	public String deleteAliasIndex(Long indexId, String indexName, Long versionId, Long clusterId)
			throws Exception {
		Long usedVersionId = indexVersionRepository.getUsedVersionByIndexIdAndClusterId(indexId, clusterId);
		
		if(usedVersionId != null){
			String deleteAliasJson = genDeleteAliasJson(indexName, usedVersionId);
			
			if(deleteAliasJson != null){
				try {
		            NStringEntity entity = new NStringEntity(deleteAliasJson, ContentType.APPLICATION_JSON);
		            return IOUtils.toString(ElasticRestClient.build(clusterRepository.selectByVersionId(versionId).getHttpAddress()).
		            		performRequest("POST", "/_aliases", Collections.emptyMap(), entity).getEntity().getContent());
		        } catch (IOException e) {
		        	logger.error(e.getClass() + " " + e.getMessage(), e);
		        }
			}
		}
		
		return null;
	}

	@Override
	public String transferAliasIndex(Long indexId, String indexName, Long targetVersionId, Long clusterId) throws Exception {
		Long usedVersionId = indexVersionRepository.getUsedVersionByIndexIdAndClusterId(indexId, clusterId);
		if (usedVersionId == null) {
			return createAliasIndex(indexName, targetVersionId);
		}

		String json = genCreateAndDeleteAliasJson(indexName, usedVersionId, targetVersionId);
		if (json != null) {
			try {
				NStringEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
				return IOUtils.toString(ElasticRestClient.build(clusterRepository.selectByVersionId(targetVersionId).getHttpAddress()).
						performRequest("POST", "/_aliases", Collections.emptyMap(), entity).getEntity().getContent());
			} catch (IOException e) {
				logger.error(e.getClass() + " " + e.getMessage(), e);
			}
		}
		return null;
	}

	private String genCreateAliasJson(String indexName, Long versionId) throws Exception{
		EsAliases esAliases = new EsAliases(); 
		List<Map<String, Map<String, String>>> actions = new ArrayList<Map<String, Map<String, String>>>(2);
		
		Map<String, Map<String, String>> addMap0 = new HashMap<String, Map<String, String>>();
		Map<String, String> addMap = new HashMap<String, String>();
		addMap.put("index", indexName + "_" + versionId);
		addMap.put("alias", indexName);
		addMap0.put("add", addMap);
		actions.add(addMap0);
		
		esAliases.setActions(actions);
		
		return JsonUtil.toJson(esAliases);
	}
	
	private String genDeleteAliasJson(String indexName, Long versionId) throws Exception{
		EsAliases esAliases = new EsAliases(); 
		List<Map<String, Map<String, String>>> actions = new ArrayList<Map<String, Map<String, String>>>(2);
		
		Map<String, Map<String, String>> removeMap0 = new HashMap<String, Map<String, String>>();
		Map<String, String> removeMap = new HashMap<String, String>();
		removeMap.put("index", indexName + "_" + versionId);
		removeMap.put("alias", indexName);
		removeMap0.put("remove", removeMap);
		actions.add(removeMap0);
		
		esAliases.setActions(actions);
		
		return JsonUtil.toJson(esAliases);
		
	}

	private String genCreateAndDeleteAliasJson(String indexName, Long originId, Long targetId) throws Exception {
		EsAliases esAliases = new EsAliases();
		List<Map<String, Map<String, String>>> actions = new ArrayList<>();

		Map<String, Map<String, String>> removeMap0 = new HashMap<>();
		Map<String, String> removeMap = new HashMap<>();
		removeMap.put("index", indexName + "_" + originId);
		removeMap.put("alias", indexName);
		removeMap0.put("remove", removeMap);
		actions.add(removeMap0);

		Map<String, Map<String, String>> addMap0 = new HashMap<>();
		Map<String, String> addMap = new HashMap<>();
		addMap.put("index", indexName + "_" + targetId);
		addMap.put("alias", indexName);
		addMap0.put("add", addMap);
		actions.add(addMap0);

		esAliases.setActions(actions);

		return JsonUtil.toJson(esAliases);
	}

	@Override
	public boolean isExistIndex(String indexName, Long versionId) {
		try {
            NStringEntity entity = new NStringEntity(genMappingJsonByVersionId(versionId), ContentType.APPLICATION_JSON);
            if(IOUtils.toString(ElasticRestClient.build(clusterRepository.selectByVersionId(versionId).getHttpAddress()).
            		performRequest("GET", "/" + indexName + "_" + versionId + "/_mapping", Collections.emptyMap(), entity).getEntity().getContent()) != null){
            	return true;
            }
        } catch (IOException e) {
        	logger.error(e.getClass() + " " + e.getMessage(), e);
        }
		
		return false;
	}

	@Override
	public Long getDataCount(String indexName, Long versionId) {
		try {
			String realIndexName = indexName + "_" + versionId;
			Cluster cluster = clusterRepository.selectByVersionId(versionId);
			return ElasticRestClient.getIndexDataCount(cluster.getHttpAddress(), realIndexName, "item");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String getIndexInfo(String indexName, Long versionId) {
		try {
			String realIndexName = indexName + "_" + versionId;
			Cluster cluster = clusterRepository.selectByVersionId(versionId);
			return ElasticRestClient.getIndexInfo(cluster.getHttpAddress(), realIndexName);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String executeDeleteByQueryDsl(Long versionId, String indexName, String dsl, int scrollSize) {
		return executeDsl("GET", versionId, indexName, dsl,
				"/" + indexName + "_" + versionId
						+ "/_delete_by_query?pretty&scroll_size=" + scrollSize
						+ "&conflicts=proceed&wait_for_completion=false");
	}

	@Override
	public String executeSearchByDsl(Long versionId, String indexName, String dsl) {
		return executeDsl("GET", versionId, indexName, dsl, "/" + indexName + "_" + versionId + "/_search?pretty");
	}

	public String executeDsl(String method, Long versionId, String indexName, String dsl, String endPoint) {
		try {
			Response response = null;
			RestClient client = ElasticRestClient
					.build(clusterRepository.selectByVersionId(versionId).getHttpAddress());
			if (dsl != null) {
				NStringEntity entity = new NStringEntity(dsl, ContentType.APPLICATION_JSON);
				response = client.performRequest(method, endPoint, Collections.emptyMap(), entity);
			} else {
				response = client.performRequest(method, endPoint, Collections.emptyMap());
			}
			return IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF-8"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public boolean excludeOneNode(String host, String nodeIp) {
		try {
			RestClient restClient = ElasticRestClient.build(host);
			String responseStr = getClusterSettings(restClient);
			 
			// add excluded node.
			String ipList = nodeIp;
			if (responseStr.indexOf("cluster.routing.allocation.exclude._ip") >= 0) {
				ipList = StringUtils.substringBetween(responseStr,"\"cluster.routing.allocation.exclude._ip\":\"","\"");
				if (StringUtils.isNotEmpty(ipList)) {
					List<String> asList = Arrays.asList(ipList.split(","));
					if (asList.contains(nodeIp)) {
						logger.info("{} is already excluded.", nodeIp);
						return true;
					}
					if (ipList.endsWith(",")) {
						ipList += nodeIp;
					} else {
						ipList += "," + nodeIp;
					}
				} else {
					ipList = nodeIp;
				}
			}
			
			return updateTransientSettings(restClient, ipList);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	@Override
	public boolean includeOneNode(String host, String nodeIp) {
		try {
			RestClient restClient = ElasticRestClient.build(host);
			String responseStr = getClusterSettings(restClient);
			 
			// add excluded node.
			String ipList = null;
			if (responseStr.indexOf("cluster.routing.allocation.exclude._ip") >= 0) {
				ipList = StringUtils.substringBetween(responseStr,"\"cluster.routing.allocation.exclude._ip\":\"","\"");
				if (StringUtils.isNotEmpty(ipList)) {
					List<String> asList = Arrays.asList(ipList.split(","));
					List<String> excludedList = new ArrayList<String>(asList);
					excludedList.remove(nodeIp);
					ipList = StringUtils.join(excludedList, ",");
					return updateTransientSettings(restClient, ipList);
				}
			} 
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	private String getClusterSettings(RestClient restClient) throws IOException {
		Response response = restClient.performRequest("GET", "/_cluster/settings?flat_settings=true",
		        Collections.singletonMap("pretty", "false"));
		return IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF-8"));
	}
	
	private boolean updateTransientSettings(RestClient restClient, String ipList) throws IOException {
		logger.info("update cluster.routing.allocation.exclude._ip to {}.", ipList);
		HttpEntity entity = new NStringEntity(  
		        "{\"transient\" : {\"cluster.routing.allocation.exclude._ip\":\"" + ipList + "\"}}", ContentType.APPLICATION_JSON);  
		Response putResponse = restClient.performRequest(  
		        "PUT",  
		        "_cluster/settings",  
		        Collections.<String, String>emptyMap(),  
		        entity);
		return 200 == putResponse.getStatusLine().getStatusCode();
	}

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
			logger.error("getExcludeNodeList by {} error: " + e, clusterAddress);
		}
		return emptyList();
	}

	@Override
	public List<String> getAvalableNodeIps(String clusterAddress) throws IOException {
		RestClient client = ElasticRestClient.build(clusterAddress);
		Response response = client.performRequest("GET", "/_cat/nodes");
		List<String> allNodes = new LinkedList<>();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("di")) {
					allNodes.add(line.substring(0, line.indexOf(' ')));
				}
			}
		}
		List<String> excludeIps = getExcludeNodeList(clusterAddress);
		return allNodes.stream().filter((String ip) -> !excludeIps.contains(ip)).collect(toList());
	}

	@Override
	public Map<String, List<String>> getShardNodesByAlias(String aliasIndexName) {
		//根据索引名找出全部索引
		List<Index> indexList = indexRepository.findByIndexName(aliasIndexName);
		if(indexList != null){
			//找出索引名找出全部物理集群并处理，产生节点集
			List<Map<String, List<String>>> shardNodeList = indexList.stream().map((Index index) -> clusterRepository.selectPhysicalClustersByIndexId(index.getId()).stream().collect(toMap(
				Cluster::getClusterId,
                (Cluster cluster) -> {
                    List<String[]> actualIndexList = getActualIndexs(cluster.getHttpAddress());
                    if (actualIndexList != null && !actualIndexList.isEmpty() ) {
                        return actualIndexList.stream()
                                .filter((String[] entry) -> aliasIndexName.equals(entry[0]))
                                .map((String[] entry) -> entry[1])
                                .collect(toList());
                    } else {
                        return new ArrayList<>(Arrays.asList(aliasIndexName));
                    }
                }
            ))).collect(toList());

			//按集群名合并节点集
			Map<String, List<String>> clusterActualIndexMap = shardNodeList.parallelStream()
				.map(Map::entrySet).flatMap(Collection::stream)
				.collect(
					Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue,
						(List<String> list1, List<String> list2) -> {
							list1.addAll(list2);
							return list1;
						}
					)
				);

			//请求每个物理集群获取分片列表并返回
			return clusterActualIndexMap.entrySet().stream().collect(toMap(
				Entry::getKey,
				(Entry<String, List<String>> r) -> {
					List<String[]> shardNodes = getIndexAndNodes(getHttpAddressByClusterName(r.getKey()));
					if(shardNodes != null && !shardNodes.isEmpty()){
						return shardNodes.stream()
								.filter((String[] shard) -> r.getValue().contains(shard[0]))
								.map((String[] shard) -> shard[1])
								.collect(toList());

					} else {
						return emptyList();
					}
				}
			));
		}
		return null;
	}

	public List<String[]> getNormalIndexs(String clusterHttpAddress) {
		return ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/shards/", (String line, List<String[]> list)->{
			String[] infos = line.split("\\s+");
			if(infos.length > 4 && ("STARTED".equals(infos[3]) || "RELOCATING".equals(infos[3])) && !infos[0].startsWith(".")){
				list.add(new String[]{infos[0]});
			}
		});
	}

	//aliasIndex -> shardNode
	public List<String[]> getIndexAndNodes(String clusterHttpAddress) {
		return ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/shards/", (String line,List<String[]> list) ->{
			String[] infos = line.split("\\s+");
			if(infos.length > 6 && ("STARTED".equals(infos[3]) || "RELOCATING".equals(infos[3]))){
				list.add(new String[]{infos[0], infos[6]});
			}
		});
	}

	//alias -> actual
	public List<String[]> getActualIndexs(String clusterHttpAddress) {
		return ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/aliases/", (String line,List<String[]> list) -> {
			String[] infos = line.split("\\s+");
			if(infos.length > 2){
				list.add(new String[]{infos[0], infos[1]});
			}
		});
	}

	@Override
	public String getClusterStatus(String clusterName) throws Exception {
		Map<String, Object> resultMap = JsonUtil.readValue(IOUtils.toString(getRestClientByClusterName(clusterName)
				.performRequest("GET", "/_cluster/health", Collections.EMPTY_MAP).getEntity().getContent(), Charset.forName("UTF-8")), Map.class);
		return (String) resultMap.get("status");
	}

	@Override
	public Map<String, String> getMainClusterSettings(String clusterName) throws Exception {
		ClusterSettings clusterSettings = JsonUtil.readValue(IOUtils.toString(getRestClientByClusterName(clusterName)
				.performRequest("GET", "/_cluster/settings", Collections.EMPTY_MAP).getEntity().getContent(), Charset.forName("UTF-8")), ClusterSettings.class);

		if(clusterSettings == null){
		    return null;
        }

		Map<String, String> map = new HashMap<>();

		try{
			map.put("cluster.routing.rebalance.enable", clusterSettings.getTransient().getCluster().getRouting().getRebalance().getEnable());
		}catch(Exception ignore){
			map.put("cluster.routing.rebalance.enable", null);
		}

		try{
			map.put("cluster.routing.allocation.enable", clusterSettings.getTransient().getCluster().getRouting().getAllocation().getEnable());
		}catch(Exception ignore){
			map.put("cluster.routing.allocation.enable", null);
		}

		Map<String, Object> allSettingsMap = JsonUtil.readValue(IOUtils.toString(getRestClientByClusterName(clusterName)
				.performRequest("GET", "/_all/_settings", Collections.EMPTY_MAP).getEntity().getContent(), Charset.forName("UTF-8")), Map.class);

		String delayedTimeout = null;
		Set<String> blockWriteIndexSet = new HashSet<>();

		for (Map.Entry<String, Object> entry: allSettingsMap.entrySet()) {
			IndexSettings indexSettings = JsonUtil.readValue(JsonUtil.toJson(entry.getValue()), IndexSettings.class);

			if(delayedTimeout == null){
				try {
					delayedTimeout = indexSettings.getSettings().getIndex().getUnassigned().getNodeLeft().getDelayedTimeout();
				}catch(Exception ignore){
					logger.error("error", ignore);
				}
			}

			IndexSettings.Blocks blocks = null;

			try{
				blocks = indexSettings.getSettings().getIndex().getBlocks();
			}catch(Exception ignore){
				logger.error("error", ignore);
			}

			if(blocks != null && "true".equals(blocks.getWrite())){
                blockWriteIndexSet.add(entry.getKey());
			}
		}

		map.put("index.unassigned.node_left.delayed_timeout", delayedTimeout);
		map.put("index.blocks.write", String.join(",", blockWriteIndexSet));

		return map;
	}

	@Override
    // nodeIp, nodeKind, nodeName
	public List<String[]> getNodes(String clusterName) throws Exception {
		return ElasticSearchStub.performRequest(getHttpAddressByClusterName(clusterName), "/_cat/nodes", (String line, List<String[]> list) -> {
			String[] infos = line.split("\\s+");
			if (infos.length > 9) {
				list.add(new String[]{infos[0], infos[7], infos[9]});
			}
		});
	}

    @Override
    // indexName, nodeIp, nodeName
    public List<String[]> getShards(String clusterName) throws Exception {
        return ElasticSearchStub.performRequest(getHttpAddressByClusterName(clusterName), "/_cat/shards/", (String line, List<String[]> list) ->{
            String[] infos = line.split("\\s+");
            if(infos.length > 6 && ("STARTED".equals(infos[3]) || "RELOCATING".equals(infos[3]))){
                list.add(new String[]{infos[0], infos[6], infos[7]});
            }
        });
    }

    public RestClient getRestClientByClusterName(String clusterName){
		return ElasticSearchStub.getElasticRestClient(getHttpAddressByClusterName(clusterName));
	}

	public String getHttpAddressByClusterName(String clusterName){
		return clusterRepository.selectByClusterName(clusterName).getHttpAddress();
	}

	@Override
	public String setIndexBlockWrite(RestClient restClient, String actualIndexName, boolean isBlockWrite) throws IOException {
		String indexBlockWriteSetting = "{\n" +
				"  \"index\": {\n" +
				"    \"blocks.write\": " + isBlockWrite + "\n" +
				"  }\n" +
				"}";

		return IOUtils.toString(restClient.performRequest("PUT", actualIndexName + "/_settings", Collections.emptyMap()
				, new NStringEntity(indexBlockWriteSetting, ContentType.APPLICATION_JSON)).getEntity().getContent());
	}

	@Override
	public String setClusterAllocation(RestClient restClient, String enable) throws IOException {
		String settings = "{\n" +
				"    \"transient\" : {\n" +
				"        \"cluster.routing.allocation.enable\" : \"" + enable + "\"\n" +
				"    }\n" +
				"}";

		return IOUtils.toString(restClient.performRequest("PUT", "/_cluster/settings", Collections.emptyMap()
				, new NStringEntity(settings, ContentType.APPLICATION_JSON)).getEntity().getContent());
	}

	@Override
	public String setClusterRebalance(RestClient restClient, String enable) throws IOException {
		String settings = "{\n" +
				"    \"transient\" : {\n" +
				"        \"cluster.routing.rebalance.enable\" : \"" + enable + "\"\n" +
				"    }\n" +
				"}";

		return IOUtils.toString(restClient.performRequest("PUT", "/_cluster/settings", Collections.emptyMap()
				, new NStringEntity(settings, ContentType.APPLICATION_JSON)).getEntity().getContent());
	}

	@Override
	public String setClusterDelayedTimeout(RestClient restClient, String timeout) throws IOException {
		String settings = "{\n" +
				"  \"settings\": {\n" +
				"    \"index.unassigned.node_left.delayed_timeout\":\"" + timeout + "\"\n" +
				"  }\n" +
				"}";

		return IOUtils.toString(restClient.performRequest("PUT", "/_all/_settings", Collections.emptyMap()
				, new NStringEntity(settings, ContentType.APPLICATION_JSON)).getEntity().getContent());
	}

    @Override
    public String setClusterFlushSynced(RestClient restClient) throws IOException {
        return IOUtils.toString(restClient.performRequest("POST", "/_flush/synced", Collections.emptyMap())
                .getEntity().getContent());
    }

	@Override
	public String cancelDeleteByQueryTask(Long versionId, String indexName, String lastMsg) {
		String result = executeDsl("GET", versionId, indexName, null, "_tasks?detailed&actions=*byquery");
		if (result != null && result.indexOf(indexName) >=0) {
			String taskidAfter = StringUtils.substringAfter(result, "tasks\"");
			if (taskidAfter == null) {
				return lastMsg + "\n no more tasks";
			}
			String taskidBetweenBrace = StringUtils.substringBetween(taskidAfter, "{", "}");
			if (taskidBetweenBrace == null) {
				return lastMsg + "\n taskidBetweenBrace is null";
			}
			String taskid = StringUtils.substringBetween(taskidAfter, "\"", "\"");
			if (taskid == null) {
				return lastMsg + "\n taskid not found";
			}
			String cancelTaskEndpoint = "_tasks/" + taskid + "/_cancel";
			logger.info("going to canel delete-by-query task, index: {}, endpoint: {}", indexName, cancelTaskEndpoint);
			String cancelResult = executeDsl("POST", versionId, indexName, null, cancelTaskEndpoint);
			logger.info("cancel task:{}, return {}", taskid, cancelResult);
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return cancelDeleteByQueryTask(versionId, indexName,
					lastMsg + "\n cancel deleteByQuery: " + taskid + " for " + indexName + " successfully");
		}
		return lastMsg + "\n no (more) deleteByQuery for " + indexName + " found";
    }
}