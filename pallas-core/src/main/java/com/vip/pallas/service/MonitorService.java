package com.vip.pallas.service;

import com.vip.pallas.bean.monitor.*;
import com.vip.pallas.exception.PallasException;

import java.util.Map;

public interface MonitorService {

    ClusterMetricInfoModel queryClusterMetrics(MonitorQueryModel queryModel) throws Exception;

    NodeMetricInfoModel queryNodeMetrics(MonitorQueryModel queryModel) throws Exception;

    IndexMetricInfoModel queryIndexMetrices(MonitorQueryModel queryModel) throws Exception;

    ClusterGaugeMetricModel queryClusterInfo(MonitorQueryModel queryModel) throws Exception;

    Map<String, NodeGaugeMetricModel> queryNodesInfo(MonitorQueryModel queryModel) throws Exception;

    Map<String, IndexGaugeMetricModel> queryIndicesInfo(MonitorQueryModel queryModel) throws Exception;
}
