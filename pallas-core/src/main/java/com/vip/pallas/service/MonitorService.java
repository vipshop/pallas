package com.vip.pallas.service;

import com.vip.pallas.bean.monitor.ClusterMetricInfoModel;
import com.vip.pallas.bean.monitor.IndexMetricInfoModel;
import com.vip.pallas.bean.monitor.MonitorQueryModel;
import com.vip.pallas.bean.monitor.NodeMetricInfoModel;
import com.vip.pallas.exception.PallasException;

public interface MonitorService {

    ClusterMetricInfoModel queryClusterMetrics(MonitorQueryModel queryModel) throws PallasException;

    NodeMetricInfoModel queryNodeMetrics(MonitorQueryModel queryModel) throws PallasException;

    IndexMetricInfoModel queryIndexMetrices(MonitorQueryModel queryModel) throws PallasException;
}
