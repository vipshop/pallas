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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.vip.pallas.bean.monitor.*;
import com.vip.pallas.bean.monitor.MonitorMetricModel.MetricModel;
import com.vip.pallas.utils.ParamConstantUtil;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.MonitorService;
import com.vip.pallas.utils.MetricConvertUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonitorServiceImpl implements MonitorService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private FreeMarkerConfigurationFactoryBean freeMarkerBean;

    private static Map<String,Integer> intervalMap = new HashMap<>();
    static {
        intervalMap.put("30s", 30);
        intervalMap.put("1m", 60);
        intervalMap.put("2m", 120);
        intervalMap.put("5m", 300);
        intervalMap.put("10m", 600);
        intervalMap.put("30m",1800);
        intervalMap.put("1h", 3600);
    }
    /**
     * 查询目标所在的类路径目录
     */
    private static final String TEMPALTE_FILE_PATH = "/templates";

    /**
     * 获取填充模板的map
     * @param queryModel
     * @return
     */
    private Map<String, Object> getDataMap(MonitorQueryModel queryModel) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("beginTime", queryModel.getFrom());
        dataMap.put("endTime", queryModel.getTo());
        dataMap.put("clusterName", queryModel.getClusterName());
        dataMap.put("interval_unit", getIntevalString(queryModel.getFrom(), queryModel.getTo()));

        return dataMap;
    }

    @Override
    public ClusterMetricInfoModel queryClusterMetrics(MonitorQueryModel queryModel) throws Exception{

        Map<String, Object> dataMap = getDataMap(queryModel);

        Cluster cluster =  getCluster(queryModel.getClusterName());
        ClusterMetricInfoModel clusterMetricInfoModel = new ClusterMetricInfoModel();

        Template templateGauge = getTempalte(ParamConstantUtil.GAUGE_STATS_TEMPLATE);
        clusterMetricInfoModel.setGaugeMetric(queryClusterInfo(templateGauge, dataMap, cluster));

        //derivative aggs
        dataMap.put("type", ParamConstantUtil.TYPE_INDICES_STATS);
        dataMap.put("isDerivative", true);

        Template templateAggs = getTempalte(ParamConstantUtil.AGGS_STATS_TEMPLATE);

        List<MetricModel<Date, Long>> searchRate = getSearchRate(templateAggs, dataMap, "indices_stats.indices_all.total.search.query_total", cluster);
        List<MetricModel<Date, Long>> indexingRate = getIndexingRate(templateAggs, dataMap, "indices_stats.indices_all.total.indexing.index_total", cluster);
        List<MetricModel<Date, Long>> searchTime = getSearchTime(templateAggs, dataMap, "indices_stats.indices_all.total.search.query_time_in_millis", cluster);
        List<MetricModel<Date, Long>> indexingTime = getIndexingTime(templateAggs, dataMap, "indices_stats.indices_all.total.indexing.index_time_in_millis", cluster);
        setLatency(searchRate, searchTime, indexingRate, indexingTime, dataMap, clusterMetricInfoModel);

        return clusterMetricInfoModel;
    }

    @Override
    public NodeMetricInfoModel queryNodeMetrics(MonitorQueryModel queryModel) throws Exception{

        Map<String, Object> dataMap = getDataMap(queryModel);

        dataMap.put("type", ParamConstantUtil.TYPE_NODE_STATS);
        dataMap.put("nodeName", queryModel.getNodeName());

        Cluster cluster =  getCluster(queryModel.getClusterName());
        NodeMetricInfoModel nodeMetricInfoModel = new NodeMetricInfoModel();

        Template templateGauge = getTempalte(ParamConstantUtil.GAUGE_STATS_TEMPLATE);

        nodeMetricInfoModel.setGaugeMetric(queryNodeInfo(templateGauge, dataMap, cluster));

        //normal aggs
        Template templateAggs = getTempalte(ParamConstantUtil.AGGS_STATS_TEMPLATE);

        dataMap.put("isDerivative", false);

        nodeMetricInfoModel.setCpuNodePercent(getNodeCpuPercent(templateAggs, dataMap, "node_stats.os.cpu.percent", cluster));
        nodeMetricInfoModel.setCpuProcessPerent(getNodeCpuPercent(templateAggs, dataMap, "node_stats.process.cpu.percent", cluster));
        nodeMetricInfoModel.setSystemLoad_1m(getSystemLoad(templateAggs, dataMap, "node_stats.os.cpu.load_average.1m", cluster));

        nodeMetricInfoModel.setJvm_heap_max_byte(getNodeJvmHeap(templateAggs, dataMap, "node_stats.jvm.mem.heap_max_in_bytes", cluster));
        nodeMetricInfoModel.setJvm_heap_used_byte(getNodeJvmHeap(templateAggs, dataMap, "node_stats.jvm.mem.heap_used_in_bytes", cluster));
        nodeMetricInfoModel.setHttpOpenCurrent(getNodeHttpOpen(templateAggs, dataMap, "node_stats.http.current_open", cluster));

        nodeMetricInfoModel.setIndex_memory_lucenc_total_byte(getNodeIndexMemory(templateAggs, dataMap, "node_stats.indices.segments.memory_in_bytes", cluster));
        nodeMetricInfoModel.setIndex_memory_terms_bytes(getNodeIndexMemory(templateAggs, dataMap, "node_stats.indices.segments.terms_memory_in_bytes", cluster));

        nodeMetricInfoModel.setSegmentCount(getNodeSegmentCount(templateAggs, dataMap, "node_stats.indices.segments.count", cluster));

        nodeMetricInfoModel.setSearchThreadpoolQueue(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.search.queue", cluster));
        nodeMetricInfoModel.setSearchThreadpoolReject(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.search.reject", cluster));
        nodeMetricInfoModel.setSearchThreadpoolThreads(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.search.threads", cluster));
        nodeMetricInfoModel.setIndexThreadpoolQueue(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.index.queue", cluster));
        nodeMetricInfoModel.setIndexThreadpoolReject(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.index.reject", cluster));
        nodeMetricInfoModel.setIndexThreadpoolThreads(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.index.threads", cluster));
        nodeMetricInfoModel.setBulkThreadpoolQueue(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.bulk.queue", cluster));
        nodeMetricInfoModel.setBulkThreadpoolReject(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.bulk.reject", cluster));
        nodeMetricInfoModel.setBulkThreadpoolThreads(getNodeThreadpool(templateAggs, dataMap, "node_stats.thread_pool.bulk.threads", cluster));
        //derivative aggs
        dataMap.put("isDerivative", true);

        nodeMetricInfoModel.setGcCountOld(getNodeGcCount(templateAggs, dataMap, "node_stats.jvm.gc.collectors.old.collection_count", cluster));
        nodeMetricInfoModel.setGcCountYoung(getNodeGcCount(templateAggs, dataMap, "node_stats.jvm.gc.collectors.young.collection_count", cluster));
        nodeMetricInfoModel.setGc_duration_old_ms(getNodeGcDuration(templateAggs, dataMap, "node_stats.jvm.gc.collectors.old.collection_time_in_millis", cluster));
        nodeMetricInfoModel.setGc_duration_young_ms(getNodeGcDuration(templateAggs, dataMap, "node_stats.jvm.gc.collectors.young.collection_time_in_millis", cluster));

        List<MetricModel<Date, Long>> searchRate = getSearchRate(templateAggs, dataMap, "node_stats.indices.search.query_total", cluster);
        List<MetricModel<Date, Long>> indexingRate = getIndexingRate(templateAggs, dataMap, "node_stats.indices.indexing.index_total", cluster);
        List<MetricModel<Date, Long>> searchTime = getSearchTime(templateAggs, dataMap, "node_stats.indices.search.query_time_in_millis", cluster);
        List<MetricModel<Date, Long>> indexingTime = getIndexingTime(templateAggs, dataMap, "node_stats.indices.indexing.index_time_in_millis", cluster);

        setLatency(searchRate, searchTime, indexingRate, indexingTime, dataMap, nodeMetricInfoModel);

        return nodeMetricInfoModel;
    }
    @Override
    public MetricInfoModel getMetricInfoModel(MonitorQueryModel queryModel) throws Exception {
        Map<String, Object> dataMap = getDataMap(queryModel);
        Template templateAggs = getTempalte(ParamConstantUtil.AGGS_STATS_TEMPLATE);
        Cluster cluster =  getCluster(queryModel.getClusterName());

        MetricInfoModel model = new MetricInfoModel();

        dataMap.put("type", ParamConstantUtil.TYPE_INDEX_STATS);
        dataMap.put("indexName", queryModel.getIndexName());
        constructMetricModel(templateAggs,dataMap,cluster,model);
        return model;
    }

    @Override
    public IndexMetricInfoModel queryIndexMetrices(MonitorQueryModel queryModel) throws Exception{

        Map<String, Object> dataMap = getDataMap(queryModel);

        //guage
        dataMap.put("type", ParamConstantUtil.TYPE_INDEX_STATS);
        dataMap.put("indexName", queryModel.getIndexName());

        Cluster cluster =  getCluster(queryModel.getClusterName());

        IndexMetricInfoModel indexMetricInfoModel = new IndexMetricInfoModel();

        Template templateGauge = getTempalte(ParamConstantUtil.GAUGE_STATS_TEMPLATE);

        indexMetricInfoModel.setGaugeMetric(queryIndexInfo(templateGauge, dataMap, cluster));

        Template templateAggs = getTempalte(ParamConstantUtil.AGGS_STATS_TEMPLATE);

        dataMap.put("isDerivative", false);

        indexMetricInfoModel.setIndex_memory_terms_in_byte(getIndex_memory(templateAggs, dataMap, "index_stats.total.segments.terms_memory_in_bytes", cluster));
        indexMetricInfoModel.setIndex_memory_lucenc_total_in_byte(getIndex_memory(templateAggs, dataMap, "index_stats.total.segments.memory_in_bytes", cluster));

        indexMetricInfoModel.setSegmentCount(getIndexSegmentCount(templateAggs, dataMap, "index_stats.total.segments.count", cluster));
        indexMetricInfoModel.setPrimarySegmentCount(getIndexSegmentCount(templateAggs, dataMap, "index_stats.primaries.segments.count", cluster));
        indexMetricInfoModel.setDocumentCount(getIndexDocumentCount(templateAggs, dataMap, "index_stats.total.docs.count", cluster));
        indexMetricInfoModel.setPrimaryDocumentCount(getIndexDocumentCount(templateAggs, dataMap, "index_stats.primaries.docs.count", cluster));
        indexMetricInfoModel.setIndex_disk_primary(getIndex_disk(templateAggs, dataMap, "index_stats.primaries.store.size_in_bytes", cluster));
        indexMetricInfoModel.setIndex_disk_total(getIndex_disk(templateAggs, dataMap, "index_stats.total.store.size_in_bytes", cluster));

        constructMetricModel(templateAggs,dataMap,cluster,indexMetricInfoModel);

        return indexMetricInfoModel;
    }

    private void constructMetricModel(Template templateAggs, Map<String, Object> dataMap, Cluster cluster,MetricInfoModel model) throws PallasException {
        dataMap.put("isDerivative", true);
        List<MetricModel<Date, Long>> searchRate = getSearchRate(templateAggs, dataMap, "index_stats.total.search.query_total", cluster);
        List<MetricModel<Date, Long>> indexingRate = getIndexingRate(templateAggs, dataMap, "index_stats.total.indexing.index_total", cluster);
        List<MetricModel<Date, Long>> searchTime = getSearchTime(templateAggs, dataMap, "index_stats.total.search.query_time_in_millis", cluster);
        List<MetricModel<Date, Long>> indexingTime = getIndexingTime(templateAggs, dataMap, "index_stats.total.indexing.index_time_in_millis", cluster);

        setLatency(searchRate, searchTime, indexingRate, indexingTime, dataMap, model);
    }


    public ClusterGaugeMetricModel queryClusterInfoRealTime(MonitorQueryModel queryModel) throws Exception {
        ClusterGaugeMetricModel gaugeMetricModel = new ClusterGaugeMetricModel();
        Cluster cluster =  getCluster(queryModel.getClusterName());
        String username = cluster.getUsername();
        String passwd = cluster.getPasswd();

        JSONObject versionJsonObj = JSONObject.parseObject( elasticSearchService.runDsl(cluster.getHttpAddress(), "/", username, passwd));
        JSONObject  healthJsonobj = JSONObject.parseObject(elasticSearchService.runDsl(cluster.getHttpAddress(), "/_cluster/health", username, passwd));
        JSONObject statsJsonobj = JSONObject.parseObject(elasticSearchService.runDsl(cluster.getHttpAddress(), "/_cluster/stats?human", username, passwd));

        gaugeMetricModel.setVersion(versionJsonObj.getJSONObject("version").getString("number"));
        gaugeMetricModel.setUnassignedShardCount(healthJsonobj.getLong("unassigned_shards"));
        gaugeMetricModel.setHealth(healthJsonobj.getString("status"));

        JSONObject indicesJsonObj = statsJsonobj.getJSONObject("indices");
        JSONObject nodesJsonObj = statsJsonobj.getJSONObject("nodes");

        gaugeMetricModel.setNodeCount(nodesJsonObj.getJSONObject("count").getInteger("total"));
        gaugeMetricModel.setIndexCount(indicesJsonObj.getLong("count"));
        gaugeMetricModel.setTotal_memory_byte(nodesJsonObj.getJSONObject("jvm").getJSONObject("mem").getLong("heap_max_in_bytes"));
        gaugeMetricModel.setUsed_memory_byte(nodesJsonObj.getJSONObject("jvm").getJSONObject("mem").getLong("heap_used_in_bytes"));
        gaugeMetricModel.setTotalShardCount(indicesJsonObj.getJSONObject("shards").getLong("total"));
        gaugeMetricModel.setDocument_store_byte(indicesJsonObj.getJSONObject("store").getLong("size_in_bytes"));
        gaugeMetricModel.setDocumentCount(indicesJsonObj.getJSONObject("docs").getLong("count"));
        gaugeMetricModel.setMax_uptime_in_millis(nodesJsonObj.getJSONObject("jvm").getLong("max_uptime_in_millis"));
        gaugeMetricModel.setMax_uptime(nodesJsonObj.getJSONObject("jvm").getString("max_uptime"));

        return gaugeMetricModel;
    }


    public List<NodeGaugeMetricModel> queryNodesInfoRealTime(MonitorQueryModel queryModel) throws Exception {

        List<NodeGaugeMetricModel> result = new ArrayList<>();
        Cluster cluster =  getCluster(queryModel.getClusterName());
        //find all node by clusterName    /_cat/nodes
        List<String[]> nodeInfos = elasticSearchService.getNodesInfos(queryModel.getClusterName());
        if(null == nodeInfos || nodeInfos.size() == 0){
            return new ArrayList<>();
        }
        //gauge metric group by nodeName
        Map<String/*nodeName*/, ShardInfoModel> shardInfoModelMap = elasticSearchService.getShardsNode(queryModel.getClusterName());
        String  metricsJsonString = elasticSearchService.runDsl(cluster.getHttpAddress(), "/_nodes/stats/fs,jvm,indices,process?human", cluster.getUsername(), cluster.getPasswd());
        JSONObject rawJsonObj = JSONObject.parseObject(metricsJsonString).getJSONObject("nodes");
        JSONObject metricsJsonObj = new JSONObject();
        rawJsonObj.forEach((k, v) -> {
            metricsJsonObj.put(rawJsonObj.getJSONObject(k).getString("name"), rawJsonObj.getJSONObject(k));
        });

        for(String[] nodeInfo: nodeInfos) {
            String nodeName = nodeInfo[9];
            if(StringUtils.isNotEmpty(queryModel.getNodeName()) && !queryModel.getNodeName().equals(nodeName)) {
                continue;
            }

            NodeGaugeMetricModel gaugeMetricModel = new NodeGaugeMetricModel();
            gaugeMetricModel.setNodeName(nodeName);
            gaugeMetricModel.setOsCpuPercent(Double.valueOf(nodeInfo[3]));
            gaugeMetricModel.setLoad_1m(Double.valueOf(nodeInfo[4]));
            gaugeMetricModel.setNodeRole(nodeInfo[7]);
            gaugeMetricModel.setMaster("*".equals(nodeInfo[8]));

            gaugeMetricModel.setTransportAddress(metricsJsonObj.getJSONObject(nodeName).getString("ip"));
            gaugeMetricModel.setProcessCpuPercent(metricsJsonObj.getJSONObject(nodeName).getJSONObject("process").getJSONObject("cpu").getDouble("percent"));
            gaugeMetricModel.setUptime_in_ms(metricsJsonObj.getJSONObject(nodeName).getJSONObject("jvm").getLong("uptime_in_millis"));
            gaugeMetricModel.setJvmHeapUsage(metricsJsonObj.getJSONObject(nodeName).getJSONObject("jvm").getJSONObject("mem").getDouble("heap_used_percent"));
            gaugeMetricModel.setAvailableFS(metricsJsonObj.getJSONObject(nodeName).getJSONObject("fs").getJSONObject("total").getLong("available_in_bytes"));
            gaugeMetricModel.setDocumentCount(metricsJsonObj.getJSONObject(nodeName).getJSONObject("indices").getJSONObject("docs").getLong("count"));
            gaugeMetricModel.setDocumentStore(metricsJsonObj.getJSONObject(nodeName).getJSONObject("indices").getJSONObject("store").getLong("size_in_bytes"));
            gaugeMetricModel.setIndexCount(shardInfoModelMap.get(nodeName).getIndexCount());
            gaugeMetricModel.setShardCount(shardInfoModelMap.get(nodeName).getTotalShards());
            gaugeMetricModel.setNodeName(nodeName);
            gaugeMetricModel.setUptime(metricsJsonObj.getJSONObject(nodeName).getJSONObject("jvm").getString("uptime"));
            result.add(gaugeMetricModel);

        }

        return result;
    }


    public List<IndexGaugeMetricModel> queryIndicesInfoRealTime(MonitorQueryModel queryModel) throws Exception {

        List<IndexGaugeMetricModel> result = new ArrayList<>();
        Cluster cluster =  getCluster(queryModel.getClusterName());
        //find all index by clusterName    /_cat/indices
        List<String[]> indexInfos = elasticSearchService.getIndexInfos(queryModel.getClusterName());
        if(null == indexInfos || indexInfos.size() == 0) {
            return result;
        }

        //gauge metric group by indexName
        Map<String/*IndexName*/, ShardInfoModel>  shardInfoModelMap = elasticSearchService.getShardsIndex(queryModel.getClusterName());
        for(String[] indexInfo : indexInfos) {
            String indexName = indexInfo[2];
            if(StringUtils.isNotEmpty(queryModel.getIndexName()) && !queryModel.getIndexName().equals(indexName)){
                continue;
            }
            IndexGaugeMetricModel gaugeMetricModel = new IndexGaugeMetricModel();
            gaugeMetricModel.setHealth(indexInfo[0]);
            gaugeMetricModel.setStatus(indexInfo[1]);
            gaugeMetricModel.setDocumentCount(Long.valueOf(indexInfo[6]));
            //gaugeMetricModel.setDocument_store_byte_total(indexInfo[8]);
            //gaugeMetricModel.setDocument_store_byte_primary(indexInfo[9]);
            gaugeMetricModel.setPrimaryShardCount(Integer.valueOf(indexInfo[4]));
            gaugeMetricModel.setReplicaShardCount(Integer.valueOf(indexInfo[5]));
            gaugeMetricModel.setTotalShardCount(gaugeMetricModel.getPrimaryShardCount() * (1 + gaugeMetricModel.getReplicaShardCount()));
            gaugeMetricModel.setUnassignedShardCount(shardInfoModelMap.get(indexName) == null ? 0: shardInfoModelMap.get(indexName).getUnassignedShards());
            gaugeMetricModel.setIndexName(indexName);
            result.add(gaugeMetricModel);

        }

        return result;
    }

    private Integer getNodeCountRealTime(String clusterName) throws Exception {
        getCluster(clusterName);
        List<String[]> nodeInfos = elasticSearchService.getNodesInfos(clusterName);
        if(null == nodeInfos || nodeInfos.size() == 0){
            return 0;
        }
        return nodeInfos.size();
    }

    private Integer getIndexCountRealTime(String clusterName) throws Exception {
        getCluster(clusterName);
        List<String[]> indexInfos = elasticSearchService.getIndexInfos(clusterName);
        if(null == indexInfos || indexInfos.size() == 0) {
            return 0;
        }
        return indexInfos.size();
    }

    @Override
    public Integer getNodeCount(MonitorQueryModel queryModel) throws Exception {
        Cluster cluster = getCluster(queryModel.getClusterName());

        Map<String, Object> dataMap = getDataMap(queryModel);
        dataMap.put("type", ParamConstantUtil.TYPE_NODE_STATS);

        Template templateListInfo = getTempalte(ParamConstantUtil.LIST_INFO_TEMPLATE);
        String fieldName = "node_stats.name";

        String string = getMetricFromES(templateListInfo, dataMap, fieldName, cluster);

        List<String> result = getNameList(string, fieldName);

        return null == result ? 0 :  result.size();
    }

    @Override
    public Integer getIndexCount(MonitorQueryModel queryModel) throws Exception {
        Cluster cluster = getCluster(queryModel.getClusterName());
        Map<String, Object> dataMap = getDataMap(queryModel);
        dataMap.put("type", ParamConstantUtil.TYPE_INDEX_STATS);

        Template templateListInfo = getTempalte(ParamConstantUtil.LIST_INFO_TEMPLATE);
        String fieldName = "index_stats.index_name";

        String string = getMetricFromES(templateListInfo, dataMap, fieldName, cluster);

        List<String> result = getNameList(string, fieldName);
        if(null == result || result.size() == 0) {
            return 0;
        }

        return result.size();
    }

    private NodeGaugeMetricModel queryNodeInfo(Template template, Map<String, Object> dataMap, Cluster cluster) throws PallasException {
        NodeGaugeMetricModel gaugeMetricModel = new NodeGaugeMetricModel();

        String result = getMetricFromES(template, dataMap, "", cluster);
        fetchNodeGaugeInfo(result, gaugeMetricModel);
        return gaugeMetricModel;

    }

    @Override
    public List<NodeGaugeMetricModel> queryNodesInfo(MonitorQueryModel queryModel) throws PallasException {
        Cluster cluster = getCluster(queryModel.getClusterName());

        Map<String, Object> dataMap = getDataMap(queryModel);
        dataMap.put("type", ParamConstantUtil.TYPE_NODE_STATS);

        Template templateListInfo = getTempalte(ParamConstantUtil.LIST_INFO_TEMPLATE);
        String fieldName = "node_stats.name";

        String string = getMetricFromES(templateListInfo, dataMap, fieldName, cluster);

        List<String> result = getNameList(string, fieldName);

        if(null == result || result.size() == 0) {
            return new ArrayList<>();
        }
        JSONObject rootObj = JSONObject.parseObject(string);
        JSONObject aggsMaxObj = rootObj.getJSONObject("aggregations").getJSONObject("aggs_max");
        String toMillsString = aggsMaxObj == null? "" : aggsMaxObj.getString("value_as_string");

        List<NodeGaugeMetricModel> nodeGaugeMetricModels = new ArrayList<>();
        for(String nodeName : result) {
            NodeGaugeMetricModel gaugeMetricModel = new NodeGaugeMetricModel();
            gaugeMetricModel.setNodeName(nodeName);
            dataMap.put("nodeName", nodeName);
            Template templateGauge = getTempalte(ParamConstantUtil.GAUGE_STATS_TEMPLATE);
            String resultString  = getMetricFromES(templateGauge, dataMap, "", cluster);

            fetchNodeGaugeInfo(resultString, gaugeMetricModel);

            nodeGaugeMetricModels.add(gaugeMetricModel);
        }

        return nodeGaugeMetricModels;


    }

    private IndexGaugeMetricModel queryIndexInfo(Template template, Map<String, Object> dataMap, Cluster cluster) throws Exception {
        IndexGaugeMetricModel gaugeMetricModel = new IndexGaugeMetricModel();

        String result = getMetricFromES(template, dataMap, "", cluster);
        fetchIndexGaugeInfo(result, gaugeMetricModel);
        return gaugeMetricModel;
    }

    @Override
    public List<IndexGaugeMetricModel> queryIndicesInfo(MonitorQueryModel queryModel) throws Exception {

        Cluster cluster = getCluster(queryModel.getClusterName());
        Map<String, Object> dataMap = getDataMap(queryModel);
        dataMap.put("type", ParamConstantUtil.TYPE_INDEX_STATS);

        Template templateListInfo = getTempalte(ParamConstantUtil.LIST_INFO_TEMPLATE);
        String fieldName = "index_stats.index_name";

        String string = getMetricFromES(templateListInfo, dataMap, fieldName, cluster);

        List<String> result = getNameList(string, fieldName);
        if(null == result || result.size() == 0) {
            return new ArrayList<>();
        }
        List<IndexGaugeMetricModel> indexGaugeMetricModels = new ArrayList<>();
        for(String indexName : result) {
            dataMap.put("indexName", indexName);
            IndexGaugeMetricModel gaugeMetricModel = new IndexGaugeMetricModel();
            gaugeMetricModel.setIndexName(indexName);
            Template templateGauge = getTempalte(ParamConstantUtil.GAUGE_STATS_TEMPLATE);
            String resultString  = getMetricFromES(templateGauge, dataMap, "", cluster);
            fetchIndexGaugeInfo(resultString, gaugeMetricModel);

            indexGaugeMetricModels.add(gaugeMetricModel);
        }

        return indexGaugeMetricModels;
    }


    private void fetchIndexGaugeInfo(String string, IndexGaugeMetricModel gaugeMetricModel) {
        JSONArray jsonArrayHits = JSON.parseObject(string).getJSONObject("hits").getJSONArray("hits");
        if(null == jsonArrayHits || jsonArrayHits.size() == 0) {
            return;
        }
        JSONObject indexStatsJsonObj = jsonArrayHits.getJSONObject(0).getJSONObject("_source").getJSONObject("index_stats");
        JSONObject primariesJsonObj = indexStatsJsonObj.getJSONObject("primaries");
        JSONObject totalJsonObj = indexStatsJsonObj.getJSONObject("total");


        gaugeMetricModel.setHealth(indexStatsJsonObj.getString("health"));
        gaugeMetricModel.setStatus(indexStatsJsonObj.getString("status"));


        gaugeMetricModel.setPrimaryShardCount(indexStatsJsonObj.getInteger("primaryShardCount"));
        gaugeMetricModel.setReplicaShardCount(indexStatsJsonObj.getInteger("replicaShardCount"));
        gaugeMetricModel.setTotalShardCount(indexStatsJsonObj.getInteger("primaryShardCount") * (1 + indexStatsJsonObj.getInteger("replicaShardCount")));
        gaugeMetricModel.setUnassignedShardCount(indexStatsJsonObj.getInteger("unassignedShardCount"));

        gaugeMetricModel.setDocumentCount(totalJsonObj.getJSONObject("docs").getLong("count"));
        gaugeMetricModel.setPrimaryDocumentCount(primariesJsonObj.getJSONObject("docs").getLong("count"));
        gaugeMetricModel.setDocument_store_byte_total(totalJsonObj.getJSONObject("store").getLong("size_in_bytes"));
        gaugeMetricModel.setDocument_store_byte_primary(primariesJsonObj.getJSONObject("store").getLong("size_in_bytes"));

    }

    private void fetchNodeGaugeInfo(String string, NodeGaugeMetricModel gaugeMetricModel) {

        JSONArray jsonArrayHits = JSON.parseObject(string).getJSONObject("hits").getJSONArray("hits");
        if(null == jsonArrayHits || jsonArrayHits.size() == 0) {
            return;
        }

        JSONObject nodeStatsJsonObj = jsonArrayHits.getJSONObject(0).getJSONObject("_source").getJSONObject("node_stats");

        gaugeMetricModel.setOsCpuPercent(nodeStatsJsonObj.getJSONObject("os").getJSONObject("cpu").getDouble("percent"));
        gaugeMetricModel.setLoad_1m(nodeStatsJsonObj.getJSONObject("os").getJSONObject("cpu").getJSONObject("load_average").getDouble("1m"));
        gaugeMetricModel.setNodeRole(nodeStatsJsonObj.getJSONArray("roles").toJSONString());
        if(nodeStatsJsonObj.getBoolean("node_master") != null && nodeStatsJsonObj.getBoolean("node_master") == true) {
            gaugeMetricModel.setMaster(true);
            gaugeMetricModel.setType("Master Node");
        } else {
            gaugeMetricModel.setMaster(false);
            gaugeMetricModel.setType("Node");
        }


        gaugeMetricModel.setTransportAddress(nodeStatsJsonObj.getString("transport_address"));
        gaugeMetricModel.setProcessCpuPercent(nodeStatsJsonObj.getJSONObject("process").getJSONObject("cpu").getDouble("percent"));
        //gaugeMetricModel.setUptime_in_ms(metricsJsonObj.getJSONObject(nodeName).getJSONObject("jvm").getLong("uptime_in_millis"));
        gaugeMetricModel.setJvmHeapUsage(nodeStatsJsonObj.getJSONObject("jvm").getJSONObject("mem").getDouble("heap_used_percent"));
        gaugeMetricModel.setAvailableFS(nodeStatsJsonObj.getJSONObject("fs").getJSONObject("total").getLong("available_in_bytes"));
        gaugeMetricModel.setDocumentCount(nodeStatsJsonObj.getJSONObject("indices").getJSONObject("docs").getLong("count"));
        gaugeMetricModel.setDocumentStore(nodeStatsJsonObj.getJSONObject("indices").getJSONObject("store").getLong("size_in_bytes"));
        gaugeMetricModel.setIndexCount(nodeStatsJsonObj.getInteger("indexCount"));
        gaugeMetricModel.setShardCount(nodeStatsJsonObj.getInteger("shardCount"));

        gaugeMetricModel.setUptime(nodeStatsJsonObj.getString("uptime"));

    }
    private ClusterGaugeMetricModel queryClusterInfo(Template template, Map<String, Object> dataMap, Cluster cluster) throws Exception {
        ClusterGaugeMetricModel gaugeMetricModel = new ClusterGaugeMetricModel();

        dataMap.put("type", ParamConstantUtil.TYPE_CLUSTER_HEALTH);
        String resultClusterHealth = getMetricFromES(template, dataMap, "", cluster);
        JSONArray jsonArrayClusterHealth = JSON.parseObject(resultClusterHealth).getJSONObject("hits").getJSONArray("hits");
        if(null == jsonArrayClusterHealth || jsonArrayClusterHealth.size() == 0) {
            return new ClusterGaugeMetricModel();
        }
        JSONObject clusterHealthJsonObj = jsonArrayClusterHealth.getJSONObject(0).getJSONObject("_source").getJSONObject("cluster_health");
        gaugeMetricModel.setUnassignedShardCount(clusterHealthJsonObj.getLong("unassigned_shards"));
        gaugeMetricModel.setHealth(clusterHealthJsonObj.getString("status"));
        gaugeMetricModel.setVersion(clusterHealthJsonObj.getString("version"));

        dataMap.put("type", ParamConstantUtil.TYPE_CLUSTER_STATS);
        String resultClusterStats = getMetricFromES(template, dataMap, "", cluster);
        JSONArray jsonArray = JSON.parseObject(resultClusterStats).getJSONObject("hits").getJSONArray("hits");
        if(null == jsonArray || jsonArray.size() == 0) {
            return new ClusterGaugeMetricModel();
        }

        JSONObject clusterStatsJsonObj = jsonArray.getJSONObject(0).getJSONObject("_source").getJSONObject("cluster_stats");

        JSONObject indicesJsonObj = clusterStatsJsonObj.getJSONObject("indices");
        JSONObject nodesJsonObj = clusterStatsJsonObj.getJSONObject("nodes");

        gaugeMetricModel.setNodeCount(nodesJsonObj.getJSONObject("count").getInteger("total"));
        gaugeMetricModel.setIndexCount(indicesJsonObj.getLong("count"));
        gaugeMetricModel.setTotal_memory_byte(nodesJsonObj.getJSONObject("jvm").getJSONObject("mem").getLong("heap_max_in_bytes"));
        gaugeMetricModel.setUsed_memory_byte(nodesJsonObj.getJSONObject("jvm").getJSONObject("mem").getLong("heap_used_in_bytes"));
        gaugeMetricModel.setTotalShardCount(indicesJsonObj.getJSONObject("shards").getLong("total"));
        gaugeMetricModel.setDocument_store_byte(indicesJsonObj.getJSONObject("store").getLong("size_in_bytes"));
        gaugeMetricModel.setDocumentCount(indicesJsonObj.getJSONObject("docs").getLong("count"));
        gaugeMetricModel.setMax_uptime(nodesJsonObj.getJSONObject("jvm").getString("max_uptime"));

        //version status
        return gaugeMetricModel;
    }


    private List<String> getNameList(String string, String fieldName) throws  PallasException{
        List<String> result = new ArrayList<>();
        JSONObject rootObj = JSON.parseObject(string);
        JSONArray jsonArray = rootObj.getJSONObject("hits").getJSONArray("hits");
        if(null == jsonArray || jsonArray.size() == 0) {
            return new ArrayList<>();
        }
        jsonArray.forEach(value -> {
            JSONObject fieldsObj = (JSONObject)value;
            String name = fieldsObj.getJSONObject("fields").getJSONArray(fieldName).getString(0);
            result.add(name);
        });

        return result;
    }

    private String getEndPoint(Map<String, Object> dataMap) {
        String indexName= ParamConstantUtil.MONITOR_INDEX_NAME;
        StringBuilder result = new StringBuilder();
        result.append("/").append(indexName).append("/_search");
        return result.toString();
    }

    public static String getIndexName(String indexName, String dateString) {
        StringBuilder result = new StringBuilder();
        result.append(indexName).append(".").append(dateString.replaceAll("-", "\\."));
        return result.toString();
    }

    /**
     *
     * gauge metric
     *
     */

    private ClusterGaugeMetricModel getClusterHealthGauge(Template template, Map<String, Object> dataMap, Cluster cluster) throws PallasException {
        ClusterGaugeMetricModel clusterGaugeMetricModel = new ClusterGaugeMetricModel();

        String result = getMetricFromES(template, dataMap, "", cluster);


        JSONArray jsonArray = JSON.parseObject(result).getJSONObject("hits").getJSONArray("hits");
        if(null == jsonArray || jsonArray.size() == 0) {
            return new ClusterGaugeMetricModel();
        }
        JSONObject clusterHealthJsonObj = jsonArray.getJSONObject(0).getJSONObject("_source").getJSONObject("cluster_health");
        clusterGaugeMetricModel.setUnassignedShardCount(clusterHealthJsonObj.getLong("unassigned_shards"));
        clusterGaugeMetricModel.setHealth(clusterHealthJsonObj.getString("status"));

       return clusterGaugeMetricModel;
    }

    private ClusterGaugeMetricModel getClusterStatsGauge(ClusterGaugeMetricModel gaugeMetricModel, Template template, Map<String, Object> dataMap, Cluster cluster) throws PallasException {

        if(null == gaugeMetricModel) {
            gaugeMetricModel = new ClusterGaugeMetricModel();
        }
        String result = getMetricFromES(template, dataMap, "", cluster);

        JSONArray jsonArray = JSON.parseObject(result).getJSONObject("hits").getJSONArray("hits");
        if(null == jsonArray || jsonArray.size() == 0) {
            return new ClusterGaugeMetricModel();
        }

        JSONObject clusterStatsJsonObj = jsonArray.getJSONObject(0).getJSONObject("_source").getJSONObject("cluster_stats");

        JSONObject indicesJsonObj = clusterStatsJsonObj.getJSONObject("indices");
        JSONObject nodesJsonObj = clusterStatsJsonObj.getJSONObject("nodes");

        gaugeMetricModel.setNodeCount(nodesJsonObj.getJSONObject("count").getInteger("total"));
        gaugeMetricModel.setIndexCount(indicesJsonObj.getLong("count"));
        gaugeMetricModel.setTotal_memory_byte(nodesJsonObj.getJSONObject("jvm").getJSONObject("mem").getLong("heap_max_in_bytes"));
        gaugeMetricModel.setUsed_memory_byte(nodesJsonObj.getJSONObject("jvm").getJSONObject("mem").getLong("heap_used_in_bytes"));
        gaugeMetricModel.setTotalShardCount(indicesJsonObj.getJSONObject("shards").getLong("total"));
        gaugeMetricModel.setDocument_store_byte(indicesJsonObj.getJSONObject("store").getLong("size_in_bytes"));
        gaugeMetricModel.setDocumentCount(indicesJsonObj.getJSONObject("docs").getLong("count"));
        gaugeMetricModel.setMax_uptime_in_millis(nodesJsonObj.getJSONObject("jvm").getLong("max_uptime_in_millis"));
        //gaugeMetricModel.setVersion("");
        return gaugeMetricModel;
    }

    /**
     *
     * cluster metric aggs
     *
     */

    private List<MetricModel<Date, Long>> getSearchRate(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster) throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsDerivative(template, dataMap,fieldName, cluster);
        return mapLong(result);

    }

    private List<MetricModel<Date, Long>> getIndexingRate(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster) throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsDerivative(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MetricModel<Date, Long>> getSearchTime(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster) throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsDerivative(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MetricModel<Date, Long>> getIndexingTime(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster) throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsDerivative(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

//    private void setIndexingLatency(List<MetricModel<Date, Long>> indexingRate, List<MetricModel<Date, Long>> indexingTime, ClusterMetricInfoModel clusterMetricInfoModel, Map<String, Object> dataMap) {
//
//        if(indexingRate!= null && indexingRate.size() > 0 && indexingTime != null && indexingTime.size() > 0) {
//            if(indexingRate.size() != indexingTime.size()) {
//                logger.error("latency error: size not equals; indexingRate size: {}, indexingTime size: {}", indexingRate.size(), indexingTime.size());
//            } else {
//                List<MetricModel<Date, Double>> result = getResult(indexingRate, indexingTime);
//                MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "ms");
//                clusterMetricInfoModel.setIndexingLatency(monitorMetricModel);
//            }
//            clusterMetricInfoModel.setIndexingRate(calculatePerSec(indexingRate, (String)dataMap.get("interval_unit")));
//        }
//        setLatency(indexingRate, indexingTime, dataMap, clusterMetricInfoModel);
//
//    }

    private void setLatency(List<MetricModel<Date, Long>> searchRate, List<MetricModel<Date, Long>> searchTime, List<MetricModel<Date, Long>> indexingRate, List<MetricModel<Date, Long>> indexingTime,Map<String, Object> dataMap, MetricInfoModel metricInfoModel) {
        if(indexingRate!= null && indexingRate.size() > 0 && indexingTime != null && indexingTime.size() > 0) {
            if(indexingRate.size() != indexingTime.size()) {
                logger.error("latency error: size not equals; indexingRate size: {}, indexingTime size: {}", indexingRate.size(), indexingTime.size());
            } else {
                List<MetricModel<Date, Double>> result = getResult(indexingRate, indexingTime);
                MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "ms");
                metricInfoModel.setIndexingLatency(monitorMetricModel);

            }
            metricInfoModel.setIndexingRate(calculatePerSec(indexingRate, (String)dataMap.get("interval_unit")));
        }

        if(searchRate!= null && searchRate.size() > 0 && searchTime != null && searchTime.size() > 0) {
            if(searchRate.size() != searchTime.size()) {
                logger.error("latency error: size not equals; searchRate size: {}, searchTime size: {}", searchRate.size(), searchTime.size());
            } else{
                List<MetricModel<Date, Double>> result = (getResult(searchRate, searchTime));
                MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>();
                monitorMetricModel.setUnit("ms");
                monitorMetricModel.setMetricModel(result);
                metricInfoModel.setSearchLatency(monitorMetricModel);
            }

            metricInfoModel.setSearchRate(calculatePerSec(searchRate,(String)dataMap.get("interval_unit")));
        }


    }
//    private void setsearchLatency(List<MetricModel<Date, Long>> searchRate, List<MetricModel<Date, Long>> searchTime, ClusterMetricInfoModel clusterMetricInfoModel, Map<String, Object> dataMap) {
//
//        if(searchRate!= null && searchRate.size() > 0 && searchTime != null && searchTime.size() > 0) {
//            if(searchRate.size() != searchTime.size()) {
//                logger.error("latency error: size not equals; searchRate size: {}, searchTime size: {}", searchRate.size(), searchTime.size());
//            } else{
//                List<MetricModel<Date, Double>> result = (getResult(searchRate, searchTime));
//                MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>();
//                monitorMetricModel.setUnit("/ms");
//                monitorMetricModel.setMetricModel(result);
//                clusterMetricInfoModel.setSearchLatency(monitorMetricModel);
//            }
//
//            clusterMetricInfoModel.setSearchRate(calculatePerSec(searchRate,(String)dataMap.get("interval_unit")));
//        }
//
//
//    }

    private MonitorMetricModel<Date, Double> calculatePerSec(List<MetricModel<Date, Long>> models, String intervalString) {
        List<MetricModel<Date, Double>> result = new ArrayList<>();
        models.forEach(value -> {
            MetricModel<Date, Double> model = new MetricModel<>();
            model.setX(value.getX());
            model.setY(value.getY() * 1.0 / intervalMap.get(intervalString));
            result.add(model);
        });
        MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "s");

        return monitorMetricModel;
    }

    private List<MetricModel<Date, Double>> getResult(List<MetricModel<Date, Long>> a, List<MetricModel<Date, Long>> b) {
        List<MetricModel<Date, Double>> result = new ArrayList<>();
        for(int i=0; i<a.size(); i++) {
            MetricModel<Date, Double> metricModel = new MetricModel<Date, Double>();
            metricModel.setX(a.get(i).getX());
            try{
                if(a.get(i).getY() == 0) {
                    metricModel.setY(0.0);
                } else {
                    metricModel.setY(b.get(i).getY() *1.0 / a.get(i).getY());
                }

            } catch (Exception e) {
                logger.error("divide error", e);
                metricModel.setY(0.0);
            }
            result.add(metricModel);
        }
        return result.stream().map(model-> {
            if (model.getY()<0) {
                model.setY(-1d);
            }
            return model;
        }).collect(Collectors.toList());
    }

    /**
     *
     * node metric aggs
     *
     */

    private  MonitorMetricModel<Date, Double> getNodeCpuPercent(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException{
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap, fieldName, cluster);
        MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "%");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Double> getSystemLoad(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException{
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap, fieldName, cluster);
        MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "1m");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Long> getNodeGcCount(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsDerivative(template, dataMap,fieldName, cluster);
        MonitorMetricModel<Date, Long> monitorMetricModel = new MonitorMetricModel<>(mapLong(result), "次");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Long> getNodeGcDuration(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsDerivative(template, dataMap,fieldName, cluster);
        MonitorMetricModel<Date, Long> monitorMetricModel = new MonitorMetricModel<>(mapLong(result), "ms");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Double> getNodeJvmHeap(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        result.forEach(value -> {
            value.setY(MetricConvertUtil.byteToMb(value.getY().longValue()));
        });
        MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "mb");
        //转换单位：double
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Integer> getNodeHttpOpen(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        MonitorMetricModel<Date, Integer> monitorMetricModel = new MonitorMetricModel<>(mapInteger(result), "");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Double> getNodeIndexMemory(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        result.forEach(value -> {
            value.setY(MetricConvertUtil.byteToMb(value.getY().longValue()));
        });

        MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "mb");
        return monitorMetricModel;
    }


    private MonitorMetricModel<Date, Long> getNodeSegmentCount(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        MonitorMetricModel<Date, Long> monitorMetricModel = new MonitorMetricModel<>(mapLong(result), "");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Integer> getNodeThreadpool(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        MonitorMetricModel<Date, Integer> monitorMetricModel = new MonitorMetricModel<>(mapInteger(result), "");
        return monitorMetricModel;
    }

    /**
     *
     * index metric aggs
     *
     */

    private MonitorMetricModel<Date, Double> getIndex_memory(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        result.forEach(value -> {
            value.setY(MetricConvertUtil.byteToMb(value.getY().longValue()));
        });

        MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "mb");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Long> getIndexSegmentCount(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        MonitorMetricModel<Date, Long> monitorMetricModel = new MonitorMetricModel<>(mapLong(result), "");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Long> getIndexDocumentCount(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        MonitorMetricModel<Date, Long> monitorMetricModel = new MonitorMetricModel<>(mapLong(result), "");
        return monitorMetricModel;
    }

    private MonitorMetricModel<Date, Double> getIndex_disk(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MetricModel<Date, Double>> result = getMonitorMetricModelsMax(template, dataMap,fieldName, cluster);
        result.forEach(value -> {
            value.setY(MetricConvertUtil.byteToMb(value.getY().longValue()));
        });

        MonitorMetricModel<Date, Double> monitorMetricModel = new MonitorMetricModel<>(result, "mb");
        return monitorMetricModel;
    }


    /**
     *
     */
    private List<MetricModel<Date, Double>> getMonitorMetricModelsDerivative(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException{
        String stringResult = getMetricFromES(template, dataMap,fieldName, cluster);
        List<MetricModel<Date, Double>> result =  parseMetricDerivative(stringResult).stream().map(model-> {
            if (model.getY()<0) {
                model.setY(-1d);
            }
            return model;
        }).collect(Collectors.toList());;
        return result;
    }

    private List<MetricModel<Date, Double>> getMonitorMetricModelsMax(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException {
        String stringResult = getMetricFromES(template, dataMap,fieldName, cluster);
        List<MetricModel<Date, Double>> result =  parseMetricMax(stringResult);
        return result;
    }


    /**
     *
     * @param template
     * @param dataMap
     * @param fieldName
     * @param cluster
     * @return
     */
    private String getMetricFromES(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster)  throws PallasException{
        StringWriter writer = new StringWriter();
        String result = "";
        if(StringUtils.isNotEmpty(fieldName)) {
            dataMap.put("fieldName", fieldName);
        }
        try {
            template.process(dataMap, writer);
            //查询模板内容
            String queryString = writer.toString();
            result = elasticSearchService.queryByDsl(queryString, getEndPoint(dataMap), cluster);
        } catch (TemplateException e) {
            logger.error("template:{} sth wrong", template.getName(), e);
            throw new PallasException("查询模板: " + template.getName() + "存在问题", e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(writer);
        }

        return result;
    }

    /**
     *
     * @param string
     * @return
     */

    private List<MetricModel<Date, Double>> parseMetricMax(String string) {
        return parseMetric(string, "aggs_1_value");
    }

    private List<MetricModel<Date, Double>> parseMetricDerivative(String string) {
        return parseMetric(string, "aggs_3_derivative");
    }

    private List<MetricModel<Date, Double>> parseMetric(String string, String aggsLevel) {
        List<MetricModel<Date, Double>> result = new ArrayList<>();

        JSONObject rootObj = JSON.parseObject(string);

        JSONArray keyValueArray = rootObj.getJSONObject("aggregations").getJSONObject("aggs_2_date_histogram").getJSONArray("buckets");

        List<JSONObject> keyValueList = keyValueArray.toJavaList(JSONObject.class);

        keyValueList.stream().forEach(jsonObject -> {
            Date date = jsonObject.getDate("key");
            JSONObject tempObj = jsonObject.getJSONObject(aggsLevel);
            Double value = tempObj == null? 0: tempObj.getDoubleValue("value");
            result.add(new MetricModel(date, value));
        });
        return result;
    }
    private static List<MetricModel<Date, Integer>> mapInteger(List<MetricModel<Date, Double>> models) {
        return Lists.transform(models, new Function<MetricModel<Date, Double>, MetricModel<Date, Integer>>() {

            @Override
            public MetricModel<Date, Integer> apply(MetricModel<Date, Double> input) {
                MetricModel<Date, Integer> model = new MetricModel<>();
                model.setX(input.getX());
                model.setY(input.getY().intValue());
                return model;
            }
        });
    }

    private static List<MetricModel<Date, Long>>mapLong(List<MetricModel<Date, Double>> models) {
        return Lists.transform(models, new Function<MetricModel<Date, Double>, MetricModel<Date, Long>>() {

            @Override
            public MetricModel<Date, Long> apply(MetricModel<Date, Double> input) {
                MetricModel<Date, Long> model = new MetricModel<>();
                model.setX(input.getX());
                model.setY(input.getY().longValue());
                return model;
            }
        });
    }

    private Cluster getCluster(String clusterId) throws PallasException{
        Cluster cluster =  clusterService.findByName(clusterId);
        if(null == cluster) {
            throw new PallasException("集群不存在：" + clusterId);
        };
        if(StringUtils.isNotEmpty(cluster.getRealClusters())) {
            throw new PallasException(clusterId + "是逻辑集群");
        }
        return cluster;
    }

    private String getIntevalString(long from , long to) {
        long dist = to - from;
        if(dist <= 1800000) {            //30m
            return "30s";
        } else if(dist <= 3600000) {    //1h
            return "30s";
        } else if(dist <= 10800000) {   //3h
            return "1m";
        } else if(dist <= 21600000) {   //6h
            return "2m";
        } else if(dist <= 43200000) {   //12h
            return "5m";
        } else if(dist <= 86400000) {   //24h
            return "10m";
        } else if(dist <= 259200000) {  //3d
            return "30m";
        }
        return "1h";
    }

    private Template getTempalte(String templateName) throws PallasException{
        Template template = null;
        try {
            template = freeMarkerBean.getObject().getTemplate(templateName);
            template.setNumberFormat("#");
        } catch (IOException e) {
            logger.error("tempalte: wrong", e);
            throw new PallasException("查询模板存在问题" , e);
        }
        return template;
    }
}
