package com.vip.pallas.utils;

public interface ConstantUtil {
    /**
     * freemarker template
     */
    String GAUGE_STATS_TEMPLATE = "gauge_stats.ftl";
    String AGGS_STATS_TEMPLATE = "aggs_stats.ftl";
    String LIST_INFO_TEMPLATE = "list_info.ftl";

    /**
     * type字段
     */
    String TYPE_CLUSTER_HEALTH = "cluster_health";
    String TYPE_CLUSTER_STATS = "cluster_stats";
    String TYPE_INDICES_STATS = "indices_stats";
    String TYPE_NODE_STATS = "node_stats";
    String TYPE_INDEX_STATS = "index_stats";

    /**
     * monitor index name
     */
    String indexName= "pallas.es.metrics";
}
