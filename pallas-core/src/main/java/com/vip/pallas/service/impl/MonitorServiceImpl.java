package com.vip.pallas.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.vip.pallas.bean.monitor.*;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.MonitorService;
import com.vip.pallas.utils.ConstantUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class MonitorServiceImpl implements MonitorService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private ClusterService clusterService;

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
        dataMap.put("cluserName", queryModel.getClusterName());
        //for the moment
        dataMap.put("interval_unit", "30s");

        return dataMap;
    }

    @Override
    public ClusterMetricInfoModel queryClusterMetrics(MonitorQueryModel queryModel) throws Exception{
        Configuration cfg = initTemplateConfiguration();
        Map<String, Object> dataMap = getDataMap(queryModel);

        //guage
        dataMap.put("type", ConstantUtil.TYPE_CLUSTER_HEALTH);

        Cluster cluster =  clusterService.findByName(queryModel.getClusterName());
        if(null == cluster) {
            throw new PallasException("集群不存在：" + queryModel.getClusterName());
        };

        ClusterMetricInfoModel clusterMetricInfoModel = new ClusterMetricInfoModel();

        clusterMetricInfoModel.setGaugeMetric(queryClusterInfo(queryModel));

        //derivative aggs
        dataMap.put("type", ConstantUtil.TYPE_INDICES_STATS);
        dataMap.put("isDerivative", true);

        Template templateAggs = null;
        try {
            templateAggs = cfg.getTemplate(ConstantUtil.AGGS_STATS_TEMPLATE);
        } catch (IOException e) {
            logger.error("tempalte: {} wrong","node_stats" ,e);
            throw new PallasException("查询模板存在问题" , e);
        }

        clusterMetricInfoModel.setSearchRate(getClusterSearchRate(templateAggs, dataMap, "indices_stats.indices_all.total.search.query_total", cluster));
        clusterMetricInfoModel.setIndexingRate(getClusterIndexingRate(templateAggs, dataMap, "indices_stats.indices_all.total.indexing.index_total", cluster));

        return clusterMetricInfoModel;
    }

    @Override
    public NodeMetricInfoModel queryNodeMetrics(MonitorQueryModel queryModel) throws Exception{
        Configuration cfg = initTemplateConfiguration();
        Map<String, Object> dataMap = getDataMap(queryModel);

        dataMap.put("type", ConstantUtil.TYPE_NODE_STATS);
        dataMap.put("nodeName", queryModel.getNodeName());

        Cluster cluster =  clusterService.findByName(queryModel.getClusterName());
        if(null == cluster) {
            throw new PallasException("集群不存在：" + queryModel.getClusterName());
        };
        //if si logic cluster

        NodeMetricInfoModel nodeMetricInfoModel = new NodeMetricInfoModel();

        nodeMetricInfoModel.setGaugeMetric(queryNodesInfo(queryModel).get(queryModel.getNodeName()));

        //normal aggs
        Template templateAggs = null;
        try {
            templateAggs = cfg.getTemplate(ConstantUtil.AGGS_STATS_TEMPLATE);
        } catch (IOException e) {
            logger.error("tempalte: wrong", e);
            throw new PallasException("查询模板存在问题" , e);
        }
        dataMap.put("isDerivative", false);

        nodeMetricInfoModel.setCpuNodePercent(getNodeCpuNodePercent(templateAggs, dataMap, "os.cpu.percent", cluster));
        nodeMetricInfoModel.setCpuProcessPerent(getNodeCpuProcessPerent(templateAggs, dataMap, "process.cpu.percent", cluster));
        nodeMetricInfoModel.setGcCountOld(getNodeGcCountOld(templateAggs, dataMap, "jvm.gc.collectors.old.collection_count", cluster));
        nodeMetricInfoModel.setGcCountYoung(getNodeGcCountYoung(templateAggs, dataMap, "jvm.gc.collectors.young.collection_count", cluster));
        nodeMetricInfoModel.setGc_duration_old_ms(getNodeGcDurationOld(templateAggs, dataMap, "jvm.gc.collectors.old.collection_time_in_millis", cluster));
        nodeMetricInfoModel.setGc_duration_young_ms(getNodeGcDurationYoung(templateAggs, dataMap, "jvm.gc.collectors.young.collection_time_in_millis", cluster));
        nodeMetricInfoModel.setJvm_heap_max_byte(getNodeJvmHeapMax(templateAggs, dataMap, "jvm.mem.heap_max_in_bytes", cluster));
        nodeMetricInfoModel.setJvm_heap_used_byte(getNodeJVmHealUsed(templateAggs, dataMap, "jvm.mem.heap_used_in_bytes", cluster));
        nodeMetricInfoModel.setHttpOpenCurrent(getNodeHttpOpen(templateAggs, dataMap, "http.current_open", cluster));

        nodeMetricInfoModel.setIndex_memory_lucenc_total_byte(getNodeIndexMemoryLucenc(templateAggs, dataMap, "indices.segments.memory_in_bytes", cluster));
        nodeMetricInfoModel.setIndex_memory_terms_bytes(getNodeIndexMemoryTerms(templateAggs, dataMap, "indices.segments.terms_memory_in_bytes", cluster));
        //nodeMetricInfoModel.setIndexingLatency();
        //nodeMetricInfoModel.setSearchLatency();
        nodeMetricInfoModel.setSegmentCount(getNodeSegmentCount(templateAggs, dataMap, "indices.segments.count", cluster));

        nodeMetricInfoModel.setSearchThreadpoolQueue(getSearchThreadpoolQueue(templateAggs, dataMap, "thread_pool.search.queue", cluster));
        nodeMetricInfoModel.setSearchThreadpoolReject(getSearchThreadpoolReject(templateAggs, dataMap, "thread_pool.search.reject", cluster));
        nodeMetricInfoModel.setIndexThreadpoolQueue(getIndexThreadpoolQueue(templateAggs, dataMap, "thread_pool.index.queue", cluster));
        nodeMetricInfoModel.setIndexThreadpoolReject(getIndexThreadpoolReject(templateAggs, dataMap, "thread_pool.index.reject", cluster));
        nodeMetricInfoModel.setBulkThreadpoolQueue(getBulkThreadpoolQueue(templateAggs, dataMap, "thread_pool.bulk.queue", cluster));
        nodeMetricInfoModel.setBulkThreadpoolReject(getBulkThreadpoolReject(templateAggs, dataMap, "thread_pool.bulk.reject", cluster));

        //derivative aggs
        dataMap.put("isDerivative", true);

        return nodeMetricInfoModel;
    }

    @Override
    public IndexMetricInfoModel queryIndexMetrices(MonitorQueryModel queryModel) throws Exception{
        Configuration cfg = initTemplateConfiguration();
        Map<String, Object> dataMap = getDataMap(queryModel);

        //guage
        dataMap.put("type", ConstantUtil.TYPE_INDEX_STATS);
        dataMap.put("indexName", queryModel.getIndexName());

        Cluster cluster =  clusterService.findByName(queryModel.getClusterName());
        if(null == cluster) {
            throw new PallasException("集群不存在：" + queryModel.getClusterName());
        };

        IndexMetricInfoModel indexMetricInfoModel = new IndexMetricInfoModel();

        indexMetricInfoModel.setGaugeMetric(queryIndicesInfo(queryModel).get(queryModel.getIndexName()));

        Template templateAggs = null;
        try {
            templateAggs = cfg.getTemplate(ConstantUtil.AGGS_STATS_TEMPLATE);
        } catch (IOException e) {
            logger.error("tempalte: wrong", e);
            throw new PallasException("查询模板存在问题" , e);
        }

        dataMap.put("isDerivative", false);

        indexMetricInfoModel.setIndex_memory_terms_in_byte(getIndex_memory_terms_in_byte(templateAggs, dataMap, "index_stats.total.segments.terms_memory_in_bytes", cluster));
        indexMetricInfoModel.setIndex_memory_lucenc_total_in_byte(getIndex_memory_lucenc_total_in_byte(templateAggs, dataMap, "index_stats.total.segments.memory_in_bytes", cluster));

        indexMetricInfoModel.setSegmentCount(getIndexSegmentCount(templateAggs, dataMap, "index_stats.total.segments.count", cluster));
        indexMetricInfoModel.setDocumentCount(getIndexDocumentCount(templateAggs, dataMap, "index_stats.total.docs.count", cluster));
        indexMetricInfoModel.setIndex_disk_primary(getIndex_disk_primary(templateAggs, dataMap, "index_stats.primaries.store.size_in_bytes", cluster));
        indexMetricInfoModel.setIndex_disk_total(getIndex_disk_total(templateAggs, dataMap, "index_stats.total.store.size_in_bytes", cluster));

        dataMap.put("isDerivative", true);
        indexMetricInfoModel.setSearchRate(getIndexSearchRate(templateAggs, dataMap, "index_stats.total.search.query_total", cluster));
        indexMetricInfoModel.setIndexingRate(getIndexIndexingRate(templateAggs, dataMap, "index_stats.total.indexing.index_total", cluster));

        return indexMetricInfoModel;
    }

    @Override
    public ClusterGaugeMetricModel queryClusterInfo(MonitorQueryModel queryModel) throws Exception {
        ClusterGaugeMetricModel gaugeMetricModel = new ClusterGaugeMetricModel();
        Cluster cluster =  clusterService.findByName(queryModel.getClusterName());
        if(null == cluster) {
            throw new PallasException("集群不存在：" + queryModel.getClusterName());
        };



        JSONObject versionJsonObj = JSONObject.parseObject( elasticSearchService.runDsl(cluster.getHttpAddress(), "/"));
        JSONObject  healthJsonobj = JSONObject.parseObject(elasticSearchService.runDsl(cluster.getHttpAddress(), "/_cluster/health"));
        JSONObject statsJsonobj = JSONObject.parseObject(elasticSearchService.runDsl(cluster.getHttpAddress(), "/_cluster/stats"));

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


        return gaugeMetricModel;
    }

    @Override
    public Map<String, NodeGaugeMetricModel> queryNodesInfo(MonitorQueryModel queryModel) throws Exception {
        Map<String, NodeGaugeMetricModel>  result = new HashMap<>();
        Cluster cluster =  clusterService.findByName(queryModel.getClusterName());
        if(null == cluster) {
            throw new PallasException("集群不存在：" + queryModel.getClusterName());
        };
        //find all node by clusterName    /_cat/nodes
        List<String[]> nodeInfos = elasticSearchService.getNodesInfos(queryModel.getClusterName());
        if(null == nodeInfos || nodeInfos.size() == 0){
            return new HashMap<>();
        }
        //gauge metric group by nodeName
        Map<String/*nodeName*/, ShardInfoModel> shardInfoModelMap = elasticSearchService.getShardsNode(queryModel.getClusterName());
        String  metricsJsonString = elasticSearchService.runDsl(cluster.getHttpAddress(), "/_nodes/stats/fs,jvm,indices,process");
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

            result.put(nodeName, gaugeMetricModel);

        }

        return result;
    }

    @Override
    public Map<String, IndexGaugeMetricModel> queryIndicesInfo(MonitorQueryModel queryModel) throws Exception {

        Map<String, IndexGaugeMetricModel> result = new HashMap<>();

        Cluster cluster =  clusterService.findByName(queryModel.getClusterName());
        if(null == cluster) {
            throw new PallasException("集群不存在：" + queryModel.getClusterName());
        };
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
            gaugeMetricModel.setDocument_store_byte_total(indexInfo[8]);
            gaugeMetricModel.setDocument_store_byte_primary(indexInfo[9]);
            gaugeMetricModel.setPrimaryShardCount(Integer.valueOf(indexInfo[4]));
            gaugeMetricModel.setReplicaShardCount(Integer.valueOf(indexInfo[5]));
            gaugeMetricModel.setTotalShardCount(gaugeMetricModel.getPrimaryShardCount() * (1 + gaugeMetricModel.getReplicaShardCount()));
            gaugeMetricModel.setUnassignedShardCount(shardInfoModelMap.get(indexName) == null ? 0: shardInfoModelMap.get(indexName).getUnassignedShards());

            result.put(indexName, gaugeMetricModel);

        }

        return result;
    }

    private String getEndPoint(Map<String, Object> dataMap) {
        String indexName= ConstantUtil.indexName;
        long beginTime = (long)dataMap.get("beginTime");
        long endTime = (long)dataMap.get("endTime");
        LocalDateTime queryEnd = LocalDateTime.ofInstant(new Date(endTime).toInstant(), ZoneId.systemDefault());
        LocalDateTime queryStart = LocalDateTime.ofInstant(new Date(beginTime).toInstant(), ZoneId.systemDefault());

        LocalDateTime queryStartZero = LocalDateTime.of( queryStart.toLocalDate(), LocalTime.MIN);
        StringBuilder result = new StringBuilder();
        result.append("/");
        result.append(getIndexName(indexName, queryStartZero.toLocalDate().toString()));
        queryStartZero = queryStartZero.plusDays(1);
        while(queryStartZero.isBefore(queryEnd)) {
            result.append(",").append(getIndexName(indexName, queryStartZero.toLocalDate().toString()));
            queryStartZero = queryStartZero.plusDays(1);
        }
        result.append("/_search");
        return result.toString();
    }

    private Configuration initTemplateConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassForTemplateLoading(this.getClass(), TEMPALTE_FILE_PATH);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setNumberFormat("#");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return cfg;
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

    private List<MonitorMetricModel<Date, Double>> getClusterSearchRate(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster) throws PallasException {
        return getMonitorMetricModels(template, dataMap,fieldName, cluster);

    }

    private List<MonitorMetricModel<Date, Double>> getClusterIndexingRate(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster) throws PallasException {
        return getMonitorMetricModels(template, dataMap,fieldName, cluster);
    }

    /**
     *
     * node metric aggs
     *
     */

    private  List<MonitorMetricModel<Date, Double>> getNodeCpuNodePercent(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException{
        return getMonitorMetricModels(template, dataMap, fieldName, cluster);
    }

    private List<MonitorMetricModel<Date, Double>> getNodeCpuProcessPerent(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException{
        return getMonitorMetricModels(template, dataMap, fieldName, cluster);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeGcCountOld(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeGcCountYoung(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeGcDurationOld(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeGcDurationYoung(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeJvmHeapMax(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeJVmHealUsed(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Integer>> getNodeHttpOpen(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapInteger(result);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeIndexMemoryLucenc(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeIndexMemoryTerms(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getNodeSegmentCount(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Integer>> getSearchThreadpoolQueue(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapInteger(result);
    }

    private List<MonitorMetricModel<Date, Integer>> getSearchThreadpoolReject(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapInteger(result);
    }

    private List<MonitorMetricModel<Date, Integer>> getIndexThreadpoolQueue(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapInteger(result);
    }

    private List<MonitorMetricModel<Date, Integer>> getIndexThreadpoolReject(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapInteger(result);
    }

    private List<MonitorMetricModel<Date, Integer>> getBulkThreadpoolQueue(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapInteger(result);
    }

    private List<MonitorMetricModel<Date, Integer>> getBulkThreadpoolReject(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapInteger(result);
    }


    /**
     *
     * index metric aggs
     *
     */

    private List<MonitorMetricModel<Date, Long>> getIndex_memory_terms_in_byte(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getIndex_memory_lucenc_total_in_byte(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getIndexSegmentCount(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getIndexDocumentCount(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getIndex_disk_primary(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private List<MonitorMetricModel<Date, Long>> getIndex_disk_total(Template template, Map<String, Object> dataMap,  String fieldName, Cluster cluster)throws PallasException {
        List<MonitorMetricModel<Date, Double>> result = getMonitorMetricModels(template, dataMap,fieldName, cluster);
        return mapLong(result);
    }

    private  List<MonitorMetricModel<Date, Double>> getIndexSearchRate(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException{
        return getMonitorMetricModels(template, dataMap, fieldName, cluster);
    }

    private  List<MonitorMetricModel<Date, Double>> getIndexIndexingRate(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException{
        return getMonitorMetricModels(template, dataMap, fieldName, cluster);
    }



    /**
     *
     */
    private List<MonitorMetricModel<Date, Double>> getMonitorMetricModels(Template template, Map<String, Object> dataMap, String fieldName, Cluster cluster) throws PallasException{
        String stringResult = getMetricFromES(template, dataMap,fieldName, cluster);
        List<MonitorMetricModel<Date, Double>> result =  parseMetric(stringResult);
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

      //  parseMetric(result, 1.0);
        return result;
    }

    /**
     *
     * @param string
     * @return
     */

    private List<MonitorMetricModel<Date, Double>> parseMetric(String string) {
       // X:Date Y:Long、Integer
        List<MonitorMetricModel<Date, Double>> result = new ArrayList<>();

        JSONObject rootObj = JSON.parseObject(string);

        JSONArray keyValueArray = rootObj.getJSONObject("aggregations").getJSONObject("aggs_2_date_histogram").getJSONArray("buckets");

        List<JSONObject> keyValueList = keyValueArray.toJavaList(JSONObject.class);

        keyValueList.stream().forEach(jsonObject -> {
            Date date = jsonObject.getDate("key");
            Double value = jsonObject.getJSONObject("aggs_1_value").getDoubleValue("value");
            result.add(new MonitorMetricModel(date, value));
        });

        return result;
    }

    private static List<MonitorMetricModel<Date, Integer>> mapInteger(List<MonitorMetricModel<Date, Double>> models) {
        return Lists.transform(models, new Function<MonitorMetricModel<Date, Double>, MonitorMetricModel<Date, Integer>>() {

            @Override
            public MonitorMetricModel<Date, Integer> apply(MonitorMetricModel<Date, Double> input) {
                MonitorMetricModel<Date, Integer> model = new MonitorMetricModel<>();
                model.setX(input.getX());
                model.setY(input.getY().intValue());
                return model;
            }
        });
    }

    private static List<MonitorMetricModel<Date, Long>> mapLong(List<MonitorMetricModel<Date, Double>> models) {
        return Lists.transform(models, new Function<MonitorMetricModel<Date, Double>, MonitorMetricModel<Date, Long>>() {

            @Override
            public MonitorMetricModel<Date, Long> apply(MonitorMetricModel<Date, Double> input) {
                MonitorMetricModel<Date, Long> model = new MonitorMetricModel<>();
                model.setX(input.getX());
                model.setY(input.getY().longValue());
                return model;
            }
        });
    }
}
