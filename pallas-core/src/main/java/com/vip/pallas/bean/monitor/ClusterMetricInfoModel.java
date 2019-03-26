package com.vip.pallas.bean.monitor;

import com.google.common.collect.Collections2;

import java.util.List;

import java.io.Serializable;
import java.util.Date;

public class ClusterMetricInfoModel implements Serializable {
    private ClusterGaugeMetricModel gaugeMetric;

    private MonitorMetricModel<Date, Double> searchRate;
    private MonitorMetricModel<Date, Double> searchTime;
    private MonitorMetricModel<Date, Double> indexingTime;
    private MonitorMetricModel<Date, Double> indexingRate;
    private MonitorMetricModel<Date, Double> searchLatency;
    private MonitorMetricModel<Date, Double> indexingLatency;

    public ClusterGaugeMetricModel getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(ClusterGaugeMetricModel gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

    public MonitorMetricModel<Date, Double> getSearchRate() {
        return searchRate;
    }

    public void setSearchRate(MonitorMetricModel<Date, Double> searchRate) {
        this.searchRate = searchRate;
    }

    public MonitorMetricModel<Date, Double> getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(MonitorMetricModel<Date, Double> searchTime) {
        this.searchTime = searchTime;
    }

    public MonitorMetricModel<Date, Double> getIndexingTime() {
        return indexingTime;
    }

    public void setIndexingTime(MonitorMetricModel<Date, Double> indexingTime) {
        this.indexingTime = indexingTime;
    }

    public MonitorMetricModel<Date, Double> getIndexingRate() {
        return indexingRate;
    }

    public void setIndexingRate(MonitorMetricModel<Date, Double> indexingRate) {
        this.indexingRate = indexingRate;
    }

    public MonitorMetricModel<Date, Double> getSearchLatency() {
        return searchLatency;
    }

    public void setSearchLatency(MonitorMetricModel<Date, Double> searchLatency) {
        this.searchLatency = searchLatency;
    }

    public MonitorMetricModel<Date, Double> getIndexingLatency() {
        return indexingLatency;
    }

    public void setIndexingLatency(MonitorMetricModel<Date, Double> indexingLatency) {
        this.indexingLatency = indexingLatency;
    }


}
