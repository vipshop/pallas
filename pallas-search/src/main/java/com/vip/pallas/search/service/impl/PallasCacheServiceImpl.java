package com.vip.pallas.search.service.impl;

import static com.vip.pallas.search.cache.RoutingCache.*;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.vip.pallas.search.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.vip.pallas.search.cache.RoutingCache;
import com.vip.pallas.search.service.PallasCacheService;

public class PallasCacheServiceImpl implements PallasCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PallasCacheServiceImpl.class);

    private static final RoutingCache ROUTING_CACHE = new RoutingCache();
    private static final String CACHE_KEY_ROUTING = "ROUTING";

    private PallasCacheServiceImpl(){
    }

    private static PallasCacheService instance = new PallasCacheServiceImpl();

    public static PallasCacheService getInstance(){
        return instance;
    }

    @Override
    public void initCache() throws ExecutionException {
        getCache();
    }

    @Override
    public void refreshRouting() {
        LoadingCache<String, Map<String, Object>> cache = ROUTING_CACHE.getCache();
        if(cache != null){
            for (String key : cache.asMap().keySet()) {
                cache.refresh(key);
            }
        }
    }

    @Override
    public void invalidateCache() {
        ROUTING_CACHE.getCache().invalidateAll();
    }

    @Override
    public List<String> getAvailableNodesByIndex(String indexName) throws ExecutionException {
        Map<String, List<String>> indexNodeListMap = getCache(INDEX_NODE_LIST);
        return indexNodeListMap != null ? indexNodeListMap.get(indexName) : null;
    }

    @Override
    public List<String> getAvailableNodesByCluster(String clusterName) throws ExecutionException {
        Map<String, List<String>> indexNodeListMap = getCache(CLUSTER_NODE_LIST);
        return indexNodeListMap != null ? indexNodeListMap.get(clusterName) : null;
    }

    @Override
    public List<IndexRouting> getIndexLevelRoutingByIndexNameAndCluster(String indexName, String clusterId) throws ExecutionException {
        Long indexId = tryToGetIndexId(indexName, clusterId);
        if (indexId == null) {
            return null;
        }
        Map<Long, List<IndexRouting>> map = getCache(INDEXLEVEL_ROUTING);
        return map != null ? map.get(indexId) : null;
    }

    @Override
    public List<IndexRouting> getClusterLevelRoutingByIndexNameAndCluster(String clusterId) throws ExecutionException {
        if (StringUtils.isEmpty(clusterId)) {
            return null;
        }
        Map<Long, List<IndexRouting>> map = getCache(CLUSTERLEVEL_ROUTING);
        return map != null ? map.get(clusterId) : null;
    }

    @Override
    public List<IndexRoutingTargetGroup> getTargetGroupByIndexId(Long indexId) throws ExecutionException {
        Map<Long, List<IndexRoutingTargetGroup>> map = getCache(INDEX_TARGET_GROUP);
        return map != null ? map.get(indexId) : null;
    }

    @Override
    public IndexRoutingTargetGroup getTargetGroupById(Long targetGroupId) throws ExecutionException {
        Map<Long, IndexRoutingTargetGroup> map = getCache(TARGET_GROUP_BY_ID);
        return map != null ? map.get(targetGroupId) : null;
    }

    @Override
    public String tryToExtractClusterId(String indexName, String providedClusterId) throws ExecutionException {
        if (!StringUtils.isEmpty(providedClusterId)) {
            return providedClusterId;
        }
        Map<String, Map<String, Index>> indexClusterMap = getCache(INDEX_CLUSTER_MAP);
        if (!indexClusterMap.containsKey(indexName)) {
            return null;
        }
        Map<String, Index> clusters = indexClusterMap.get(indexName);
        if (clusters.size() == 1) {
            return clusters.values().iterator().next().getClusterName();
        } else {
            return null;
        }
    }

    private Long tryToGetIndexId(String indexName, String clusterId) throws ExecutionException {
        Map<String, Map<String, Index>> indexClusterMap = getCache(INDEX_CLUSTER_MAP);
        if (!indexClusterMap.containsKey(indexName)) {
            return null;
        }
        Map<String, Index> clusters = indexClusterMap.get(indexName);
        if (clusters.containsKey(clusterId)) {
            return clusters.get(clusterId).getId();
        } else if (clusters.size() == 1) {
            //如果找不到指定的逻辑Cluster的话，而我们Pallas Search又只有一个indexName的记录的话，直接返回该记录而不需要指定cluster
            return clusters.values().iterator().next().getId();
        } else {
            return null;
        }
    }

    @Override
    public String getClusterPortByIndexAndCluster(String indexName, String clusterName) {
        try {
            Map<String, Map<String, String>> map = getCache(INDEX_CLUSTER_PORT);
            return map.get(indexName).get(clusterName);
        } catch (ExecutionException e) {
            LOGGER.error(e.toString(), e);
        }
        return "9200";
    }

    @Override
    public String getClusterPortByClusterName(String clusterName) {
        try {
            Map<String, String> cacheMap = getCache(CLUSTER_PORT);
            return cacheMap != null ? cacheMap.get(clusterName) : null;
        } catch (ExecutionException e) {
            LOGGER.error(e.toString(), e);
        }
        return "9200";
    }

	@Override
	public List<FlowRecord> getFlowRecord(String clusterName, String indexName, String templateName)
			throws ExecutionException {
		Map<String, Map<String, Map<String, List<FlowRecord>>>> flowRecordByIndexAndTemplateAndClusterMap = getCache(FLOW_RECORD_MAP);
		if(flowRecordByIndexAndTemplateAndClusterMap != null && flowRecordByIndexAndTemplateAndClusterMap.containsKey(indexName)){
			Map<String, Map<String, List<FlowRecord>>> flowRecordTemplateAndClusterMap = flowRecordByIndexAndTemplateAndClusterMap.get(indexName);
			if(flowRecordTemplateAndClusterMap != null) {
                List<FlowRecord> recordList = null;

				if(flowRecordTemplateAndClusterMap.containsKey(templateName)) {
                    List<FlowRecord> list = flowRecordTemplateAndClusterMap.get(templateName).get(clusterName);
                    if(list != null){
                        recordList = new ArrayList<>();
                        recordList.addAll(list);
                    }
				}

				if(flowRecordTemplateAndClusterMap.containsKey("-")) {
                    List<FlowRecord> list = flowRecordTemplateAndClusterMap.get("-").get(clusterName);
                    if(list != null){
                        if(recordList == null){
                            recordList = new ArrayList<>();
                        }
                        recordList.addAll(list);
                    }
				}

				if (recordList != null && recordList.size() > 0) {
                    return recordList;
				}
			}
		}

		return null;
	}

	@Override
	public FlowRecord getFlowRecordById(Long recordId) throws ExecutionException {
		Map<Long, FlowRecord> flowRecordById = getCache(FLOW_RECORD_MAP_BY_ID);
		return flowRecordById.get(recordId);
	}

	@Override
    public List<String> getAllNodeListByClusterName(String clusterName) throws ExecutionException {
        Map<String, List<String>> allNodeListMap = getCache(CLUSTER_NODE_LIST);
        return allNodeListMap != null ? allNodeListMap.get(clusterName) : null;
    }

    @Override
    public List<String> getShardNodeListByIndexAndCluster(String indexName, String clusterName) throws ExecutionException {
        Map<String, Map<String, List<String>>> clusterShardNodeListMap = getCache(SHARD_NODE_LIST);
        if(clusterShardNodeListMap != null){
            List<String> indexList = getSourceIndexByIndexAndCluster(indexName, clusterName);
            if(indexList != null){
                return indexList.stream()
                        .filter(index -> clusterShardNodeListMap.get(index) != null)
                        .filter(index -> clusterShardNodeListMap.get(index).get(clusterName) != null)
                        .map(index -> clusterShardNodeListMap.get(index).get(clusterName).stream().collect(toList()))
                        .flatMap(list -> list.stream())
                        .collect(toList());
            }else{
                Map<String, List<String>> shardNodeListMap = clusterShardNodeListMap.get(indexName);
                return shardNodeListMap != null && shardNodeListMap.containsKey(clusterName) ? shardNodeListMap.get(clusterName) : null;
            }
        } else {
            return null;
        }
    }

    @Override
    public List<String> getSourceIndexByIndexAndCluster(String indexName, String clusterName) throws ExecutionException {
        Map<String, Map<String, List<String>>> clusterAliaseIndexMap = getCache(ALIASE_INDEX_MAP);
        if(clusterAliaseIndexMap != null){
            Map<String, List<String>> indexMap = clusterAliaseIndexMap.get(indexName);
            return indexMap != null && indexMap.containsKey(clusterName) ? indexMap.get(clusterName) : null;
        } else {
            return null;
        }
    }

    private Map<String, Object> getCache() throws ExecutionException {
        return ROUTING_CACHE.getCache().get(CACHE_KEY_ROUTING);
    }

    private <T> T getCache(String key) throws ExecutionException {
        return (T)getCache().get(key);
    }

	@Override
	public TemplateWithTimeoutRetry getConfigByTemplateIdAndCluster(String templateId, String clusterName, String indexName) throws ExecutionException {
		Map<String, TemplateWithTimeoutRetry> configMap = getCache(TEMPLATE_RETRYTIMEOUT_CONFIG);
        TemplateWithTimeoutRetry timeoutRetry = configMap != null? configMap.get(clusterName + "_" + templateId):null;
        return timeoutRetry != null ? timeoutRetry : configMap.get(clusterName + "_" + indexName);
	}

    @Override
    public Index getIndexByIndexAndCluster(String indexName, String clusterName){
        try {
            Map<String, Map<String, Index>> indexClusterMap = getCache(INDEX_CLUSTER_MAP);
            if(indexClusterMap != null){
                Map<String, Index> indexMap = indexClusterMap.get(indexName);
                return indexMap != null && indexMap.containsKey(clusterName) ? indexMap.get(clusterName) : null;
            }
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        return null;
    }

    @Override
    public SearchAuthorization getSearchAuthorization(String clientToken) throws ExecutionException {
        Map<String, SearchAuthorization> cacheMap = getCache(INDEX_ROUTING_AUTHORIZATION);
        return cacheMap != null? cacheMap.get(clientToken) : null;
    }


}
