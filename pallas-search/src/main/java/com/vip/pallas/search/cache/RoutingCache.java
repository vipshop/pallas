package com.vip.pallas.search.cache;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.model.*;
import com.vip.pallas.search.service.ElasticSearchService;
import com.vip.pallas.search.service.impl.ElasticSearchServiceImpl;
import com.vip.pallas.search.utils.ElasticRestClient;
import com.vip.pallas.search.utils.HttpClient;
import com.vip.pallas.search.utils.JsonUtil;
import com.vip.pallas.utils.PallasBasicProperties;

public class RoutingCache extends AbstractCache<String, Map<String, Object>> {

    public static final String INDEX_NODE_LIST = "INDEX_NODE_LIST";
    public static final String CLUSTER_NODE_LIST = "CLUSTER_NODE_LIST";
    public static final String SHARD_NODE_LIST = "SHARD_NODE_LIST";
    public static final String ALIASE_INDEX_MAP = "ALIASE_INDEX_MAP"; //索引别名
    public static final String INDEXLEVEL_ROUTING = "INDEXLEVEL_ROUTING";
    public static final String CLUSTERLEVEL_ROUTING = "CLUSTERLEVEL_ROUTING";
    public static final String INDEX_TARGET_GROUP = "INDEX_TARGET_GROUP";
    public static final String INDEX_ROUTING_AUTHORIZATION = "INDEX_ROUTING_AUTHORIZATION";
    public static final String TARGET_GROUP_BY_ID = "TARGET_GROUP_BY_ID";
    public static final String INDEX_CLUSTER_PORT = "INDEX_CLUSTER_PORT";
    public static final String CLUSTER_PORT = "CLUSTER_PORT";
    public static final String TEMPLATE_RETRYTIMEOUT_CONFIG = "TEMPLATE_RETRYTIMEOUT_CONFIG";
    public static final String INDEX_CLUSTER_MAP = "INDEX_CLUSTER_MAP";
	public static final String FLOW_RECORD_MAP = "FLOW_RECORD_MAP";
	public static final String FLOW_RECORD_MAP_BY_ID = "FLOW_RECORD_MAP_BY_ID";

    protected static final Pattern CLUSTER_HTTP_PORT_PATTERN = Pattern.compile(".*:([0-9]+).*");
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingCache.class);
    private static ElasticSearchService elasticSearchService = new ElasticSearchServiceImpl();

    private volatile Map<String, Object> lastCache;

    public RoutingCache() {
        super();
        super.init();
    }

    @Override
    final protected Map<String, Object> fetchData(String key) {
        try {
            Map<String, Object> cacheMap = new HashMap<>();
            cacheNode(cacheMap);
            cacheRouting(cacheMap);
            cacheTargetGroup(cacheMap);
            cacheAuthorization(cacheMap);
            cacheTimeoutConfig(cacheMap);
			cacheFlowRecord(cacheMap);
            
            lastCache = cacheMap;
            return cacheMap;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return lastCache;
        }
    }

	private void cacheFlowRecord(Map<String, Object> cacheMap) throws Exception {
		Map<String, Object> _flowRecordMap = JsonUtil.readValue(
				HttpClient.httpGet(
						PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/record/flow_record/available/list.json"),
				Map.class);
		List<FlowRecord> flowRecordList = JsonUtil.readValue(JsonUtil.toJson(_flowRecordMap.get("data")), List.class,
				FlowRecord.class);

		Map<Long, FlowRecord> flowRecordById = flowRecordList.stream().collect(toMap(FlowRecord::getId, identity()));

		Map<String, List<FlowRecord>> flowRecordByIndexMap = flowRecordList.stream().collect(
				groupingBy(i -> i.getFlowRecordConfig().getIndex().getIndexName(), mapping(identity(), toList())));

		// "-"表示全部模板
		Map<String, Map<String, List<FlowRecord>>> flowRecordByIndexAndTemplateMap = flowRecordByIndexMap.entrySet()
				.stream()
				.collect(toMap(entry -> entry.getKey(), entry -> entry.getValue().stream()
						.filter(i -> i.getFlowRecordConfig().getTemplate() != null)
						.collect(groupingBy(
								i -> !"-".equals(i.getFlowRecordConfig().getTemplate().getTemplateName())
										? i.getFlowRecordConfig().getIndex().getIndexName() + "_"
												+ i.getFlowRecordConfig().getTemplate().getTemplateName()
										: "-",
								mapping(identity(), toList())))));

		Map<String, Map<String, Map<String, List<FlowRecord>>>> flowRecordByIndexAndTemplateAndClusterMap = flowRecordByIndexAndTemplateMap
				.entrySet().stream().collect(
						toMap(entry -> entry.getKey(),
								entry -> entry.getValue().entrySet().stream()
										.collect(toMap(entry1 -> entry1.getKey(),
												entry1 -> entry1.getValue().stream().collect(groupingBy(
														i -> i.getFlowRecordConfig().getCluster().getClusterId(),
														mapping(identity(), toList())))))));

		cacheMap.put(FLOW_RECORD_MAP_BY_ID, flowRecordById);

		// key: index_template_cluster, value: FlowRecord
		cacheMap.put(FLOW_RECORD_MAP, flowRecordByIndexAndTemplateAndClusterMap);
	}

	private void cacheAuthorization(Map<String, Object> cacheMap) throws Exception {
		Map<String, Object> resultMap = JsonUtil.readValue(
				HttpClient.httpGet(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/route/index_routing_authorization/all.json"), Map.class);
		List<SearchAuthorization> list = JsonUtil.readValue(JsonUtil.toJson(resultMap.get("data")), List.class,
				SearchAuthorization.class);

		if (list != null) {
			cacheMap.put(INDEX_ROUTING_AUTHORIZATION,
					list.stream().collect(toMap(p -> p.getClientToken(), identity())));
        }

    }

    private void cacheNode(Map<String, Object> cacheMap) throws Exception {
        Map<String, Object> indexResultMap = JsonUtil.readValue(HttpClient.httpGet(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/index/list/all.json"), Map.class);
        List<Index> indexList = JsonUtil.readValue(JsonUtil.toJson(indexResultMap.get("data")), List.class, Index.class);

        Map<String, Object> clusterResultMap = JsonUtil.readValue(HttpClient.httpGet(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/cluster/all/physicals.json"), Map.class);
        List<Cluster> clusterList = JsonUtil.readValue(JsonUtil.toJson(((Map) clusterResultMap.get("data")).get("list")), List.class, Cluster.class);
        Map<String, List<String>> logicalPhysicalMap = (Map)((Map) clusterResultMap.get("data")).get("logic_physical_map");

        //clusterId -> cluster
        Map<String, Cluster> clusterMap = clusterList.stream().collect(toMap(Cluster::getClusterId, cluster -> cluster));

        //indexName -> clusterName -> index
        Map<String, Map<String, Index>> indexClusterMap = indexList.stream().collect(groupingBy(Index::getIndexName)).entrySet().stream().collect(toMap(
                 Map.Entry::getKey,
                 r -> r.getValue().stream().collect(toMap(Index::getClusterName, identity()))
            ));

        //indexName -> clusterNameSet
        Map<String, Set<String>> indexClusterSetMap = new HashMap<>();
        for (Index index : indexList) {
            indexClusterSetMap.compute(index.getIndexName(),
                    (k, v) -> {
                        v = v == null ? new HashSet<>() : v;
                        String c = index.getClusterName();
                        if (clusterMap.containsKey(c)) {
                            v.add(c);
                        } else if (logicalPhysicalMap.containsKey(c)) {
                            v.addAll(logicalPhysicalMap.get(c));
                        }
                        return v;
                    });
        }

        //index -> clusterName -> clusterPort
        Map<String, Map<String, String>> indexClusterPortMap = indexClusterSetMap.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            r -> r.getValue().stream()
                .collect(toMap(
                    clusterName -> clusterName,
                    clusterName -> {
                        Cluster cluster = clusterMap.get(clusterName);
                        if (cluster != null) {
                            Matcher matcher = CLUSTER_HTTP_PORT_PATTERN.matcher(cluster.getHttpAddress());
                            if (matcher.find()) {
                                return matcher.group(1);
                            }
                        }
                        return "9200";
                    }
                ))
        ));

        Map<String, String> clusterPortMap = clusterList.stream().collect(toMap(Cluster::getClusterId, cluster -> {
            Matcher matcher = CLUSTER_HTTP_PORT_PATTERN.matcher(cluster.getHttpAddress());
            return matcher.find() ? matcher.group(1) : "9200";
        }));

        //clusterId -> nodeList
        Map<String, List<String>> clusterNodeListMap = clusterList.stream().collect(toMap(
                Cluster::getClusterId,
                cluster -> {
                    List<String> nodeList = getNodeList(cluster.getHttpAddress());
                    if (nodeList != null) {
                        nodeList.removeAll(elasticSearchService.getExcludeNodeList(cluster.getHttpAddress()));
                        nodeList.removeAll(getAbnormalNodeList(cluster.getClusterId()));
                        return Optional.ofNullable(nodeList).orElseGet(Collections::emptyList);
                    }
					// if sth. goes wrong, keep the former cache rather than the null value.
					LOGGER.info("getNodeList by {} returns null, keep the former values in cache.",
							cluster.getHttpAddress());
					Map<String, List<String>> clusterNodeListMapInCache = (Map<String, List<String>>) cacheMap
							.get(CLUSTER_NODE_LIST);
					if (clusterNodeListMapInCache != null) {
						List<String> nodeListInCache = clusterNodeListMapInCache.get(cluster.getHttpAddress());
						return nodeListInCache == null ? emptyList() : nodeListInCache;
					}
					return emptyList();
                }
        ));

        //index -> nodeList
        Map<String, List<String>> indexNodeListMap = indexClusterSetMap.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            r -> r.getValue().stream()
                .map(clusterName -> Optional.ofNullable(clusterNodeListMap.get(clusterName)).orElseGet(Collections::emptyList))
                .collect(toList()).stream().flatMap(Collection::stream).collect(toList())
        ));

		// index -> clusterId -> shardNodeListMap
		Map<String, Map<String, List<String>>> clusterShardNodeListMap = reverseMapKey(
				clusterList.stream().collect(toMap(Cluster::getClusterId, cluster -> {
					List<String[]> shardNodeList = elasticSearchService.getIndexAndNodes(cluster.getHttpAddress());
					if (shardNodeList != null) {
						shardNodeList.removeAll(elasticSearchService.getExcludeNodeList(cluster.getHttpAddress()));
						shardNodeList.removeAll(getAbnormalNodeList(cluster.getClusterId()));
						return Optional
								.ofNullable(shardNodeList.stream()
										.collect(groupingBy(t -> t[0], mapping(t -> t[1], toList()))))
								.orElseGet(Collections::emptyMap);
					}
					// if sth. goes wrong, keep the former cache rather than the null value.
					LOGGER.info("getShards by {} returns null, keep the former values in cache.",
							cluster.getHttpAddress());
					Map<String, Map<String, List<String>>> clusterShardNodeListMapInCache = (Map<String, Map<String, List<String>>>) cacheMap
							.get(SHARD_NODE_LIST);
					if (clusterShardNodeListMapInCache != null) {
						Map<String, List<String>> shardNodeListInCache = clusterShardNodeListMapInCache
								.get(cluster.getHttpAddress());
						return shardNodeListInCache == null ? emptyMap() : shardNodeListInCache;
					}
					return emptyMap();
				})));

        //aliaseIndex -> clusterId -> indexList
        Map<String, Map<String, List<String>>> clusterAliaseIndexMap = reverseMapKey(clusterList.stream().collect(toMap(
            cluster -> cluster.getClusterId(),
            cluster -> {
                List<String[]> aliasesList = elasticSearchService.getActualIndexs(cluster.getHttpAddress());
                return aliasesList != null ? Optional.ofNullable(aliasesList.stream().
                        collect(groupingBy(i -> i[0], mapping(i -> i[1], toList())))).orElseGet(Collections::emptyMap) : emptyMap();
            }
        )));

        cacheMap.put(INDEX_NODE_LIST, indexNodeListMap);
        cacheMap.put(CLUSTER_NODE_LIST, clusterNodeListMap);
        cacheMap.put(SHARD_NODE_LIST, clusterShardNodeListMap);
        cacheMap.put(INDEX_CLUSTER_PORT, indexClusterPortMap);
        cacheMap.put(CLUSTER_PORT, clusterPortMap);
        cacheMap.put(ALIASE_INDEX_MAP, clusterAliaseIndexMap);
        cacheMap.put(INDEX_CLUSTER_MAP, indexClusterMap);
    }

	private void cacheTimeoutConfig(Map<String, Object> cacheMap) throws Exception {
		Map<String, Object> resultMap = JsonUtil.readValue(
				HttpClient.httpGet(
						PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/template/retrytimeout_configured.json"),
				Map.class);
		List<TemplateWithTimeoutRetry> configList = JsonUtil.readValue(JsonUtil.toJson(resultMap.get("data")),
				List.class, TemplateWithTimeoutRetry.class);

		Map<String, TemplateWithTimeoutRetry> configMap = null;

		if (configList != null && !configList.isEmpty()) {
			configMap = configList.stream().collect(Collectors
					.toMap(p -> p.getClusterName() + "_" + p.getIndexName() + "_" + p.getTemplateName(), p -> p));
			cacheMap.put(TEMPLATE_RETRYTIMEOUT_CONFIG, configMap);
		}

		if (configMap == null) {
			configMap = new HashMap<>();
		}

		Map<String, Object> indexResultMap = JsonUtil.readValue(
				HttpClient.httpGet(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/index/list/all.json"), Map.class);
		List<Index> indexList = JsonUtil.readValue(JsonUtil.toJson(indexResultMap.get("data")), List.class,
				Index.class);

		for (Index index : indexList) {
			TemplateWithTimeoutRetry retry = new TemplateWithTimeoutRetry();
			retry.setClusterName(index.getClusterName());
			retry.setIndexId(index.getId());
			retry.setRetry(index.getRetry());
			retry.setTemplateId(-1L);
			retry.setIndexName(index.getIndexName());
			retry.setTimeout(index.getTimeout());

			if (retry.getRetry() == null) {
				retry.setRetry(0);
			}

			if (retry.getTimeout() == null) {
				retry.setTimeout(0);
			}

			configMap.put(retry.getClusterName() + "_" + retry.getIndexName(), retry);
		}
	}
    private void cacheRouting(Map<String, Object> cacheMap){
        try {
            Map<String, Object> resultMap = JsonUtil.readValue(HttpClient.httpGet(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/route/index_routing/all.json"), Map.class);
            List<IndexRouting> routingList = JsonUtil.readValue(JsonUtil.toJson(resultMap.get("data")), List.class, IndexRouting.class);

            if (routingList != null) {
                routingList.stream().forEach(indexRouting -> {
                    try {
                        indexRouting.setConditionList(IndexRouting.fromXContent(indexRouting.getRoutingsInfo()));
                    } catch (Exception e) {
                        LOGGER.error(e.toString(), e);
                    }
                });
                cacheMap.put(INDEXLEVEL_ROUTING, routingList.stream().filter(r -> r.getType().equals(IndexRouting.ROUTE_TYPE_INDEX)).collect(groupingBy(IndexRouting::getIndexId)));
                cacheMap.put(CLUSTERLEVEL_ROUTING, routingList.stream().filter(r -> r.getType().equals(IndexRouting.ROUTE_TYPE_CLUSTER)).collect(groupingBy(IndexRouting::getIndexName)));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void cacheTargetGroup(Map<String, Object> cacheMap) throws Exception {
        Map<String, Object> resultMap = JsonUtil.readValue(HttpClient.httpGet(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/route/index_routing_target_group/all.json"), Map.class);
        List<IndexRoutingTargetGroup> targetGroupList = JsonUtil.readValue(JsonUtil.toJson(resultMap.get("data")), List.class, IndexRoutingTargetGroup.class);

        if (targetGroupList != null) {
            targetGroupList.stream().forEach(targetGroup -> {
                try {
                    targetGroup.setNodeInfoList(IndexRoutingTargetGroup.fromXContent(targetGroup.getNodesInfo()));
                    targetGroup.setClusterInfoList(IndexRoutingTargetGroup.fromClusterContent(targetGroup.getClustersInfo()));
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            });
            cacheMap.put(INDEX_TARGET_GROUP, targetGroupList.stream().collect(groupingBy(IndexRoutingTargetGroup::getIndexId)));
            cacheMap.put(TARGET_GROUP_BY_ID, targetGroupList.stream().collect(toMap(IndexRoutingTargetGroup::getId, targetGroup -> targetGroup)));
        }
    }

    private List<String> getNodeList(String clusterHttpAddress) {
        RestClient client = ElasticRestClient.build(clusterHttpAddress);
        Response response;
        try {
            response = client.performRequest("GET", "/_cat/nodes");
        } catch (IOException e) {
			LOGGER.error("getNodeList error by clusterHttpAddress: {}, cause: " + e.getMessage(), clusterHttpAddress);
            return null;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            String line;
            List<String> nodeList = new LinkedList<>();
            while ((line = br.readLine()) != null) {
                if (line.contains("di") || line.contains("mdi")) {
                    int idx = line.indexOf(' ');
                    idx = idx == -1 ? 0 : idx;
                    nodeList.add(line.substring(0, idx));
                }
            }
            return nodeList;
        } catch (Exception e) {
			LOGGER.error("getNodeList error by clusterHttpAddress: {}, cause: " + e.getMessage(), clusterHttpAddress);
            return null;
        }
    }

    //调转两层嵌套Map的Key
    private static <K, V> Map<K, Map<K, V>> reverseMapKey(Map<K, Map<K, V>> map){
        Map<K, Map<K, V>> newMap = new HashMap<>();
        map.forEach((k, v) -> v.forEach((k1, v1) -> {
            if(!newMap.containsKey(k1)){
                newMap.put(k1, new HashMap<>());
            }
            newMap.get(k1).put(k, v1);
        }));
        return newMap;
    }

    public List<String> getAbnormalNodeList(String clusterName){
        try{
            Map<String, Object> resultMap = JsonUtil.readValue(HttpClient.httpPost(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/cluster/abnormal_node/list.json", "{\"clusterName\": \""+ clusterName +"\"}"), Map.class);
            List<Node> abnormalNodeList = JsonUtil.readValue(JsonUtil.toJson(((Map) resultMap.get("data")).get("list")), List.class, Node.class);
            if(abnormalNodeList != null){
                return abnormalNodeList.stream().map(node -> node.getNodeIp()).distinct().collect(toList());
            }
        } catch (Exception ignore){
            LOGGER.error("error", ignore);
        }
        return Collections.EMPTY_LIST;
    }
}
