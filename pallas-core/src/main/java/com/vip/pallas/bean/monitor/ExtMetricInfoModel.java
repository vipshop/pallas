package com.vip.pallas.bean.monitor;

public class ExtMetricInfoModel {
    private MetricInfoModel metric;
    private String clusterName;

    public ExtMetricInfoModel() {
    }

    public ExtMetricInfoModel(MetricInfoModel metric, String clusterName) {
        this.metric = metric;
        this.clusterName = clusterName;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public MetricInfoModel getMetric() {
        return metric;
    }

    public void setMetric(MetricInfoModel metric) {
        this.metric = metric;
    }
}
