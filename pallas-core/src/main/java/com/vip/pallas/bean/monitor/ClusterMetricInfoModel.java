package com.vip.pallas.bean.monitor;

import java.util.List;

import java.io.Serializable;
import java.util.Date;

public class ClusterMetricInfoModel implements Serializable {
    private ClusterGaugeMetricModel gaugeMetric;

    private List<MonitorMetricModel<Date, Double>> searchRate;
    private List<MonitorMetricModel<Date, Double>> searchLatency;
    private List<MonitorMetricModel<Date, Double>> indexingRate;
    private List<MonitorMetricModel<Date, Double>> indexingLatency;

    public ClusterGaugeMetricModel getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(ClusterGaugeMetricModel gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

    public List<MonitorMetricModel<Date, Double>> getSearchRate() {
        return searchRate;
    }

    public void setSearchRate(List<MonitorMetricModel<Date, Double>> searchRate) {
        this.searchRate = searchRate;
    }

    public List<MonitorMetricModel<Date, Double>> getSearchLatency() {
        return searchLatency;
    }

    public void setSearchLatency(List<MonitorMetricModel<Date, Double>> searchLatency) {
        this.searchLatency = searchLatency;
    }

    public List<MonitorMetricModel<Date, Double>> getIndexingRate() {
        return indexingRate;
    }

    public void setIndexingRate(List<MonitorMetricModel<Date, Double>> indexingRate) {
        this.indexingRate = indexingRate;
    }

    public List<MonitorMetricModel<Date, Double>> getIndexingLatency() {
        return indexingLatency;
    }

    public void setIndexingLatency(List<MonitorMetricModel<Date, Double>> indexingLatency) {
        this.indexingLatency = indexingLatency;
    }
}
