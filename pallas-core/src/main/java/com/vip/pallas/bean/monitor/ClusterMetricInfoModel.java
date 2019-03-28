package com.vip.pallas.bean.monitor;

import java.io.Serializable;

public class ClusterMetricInfoModel extends MetricInfoModel implements Serializable{
    private ClusterGaugeMetricModel gaugeMetric;

    public ClusterGaugeMetricModel getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(ClusterGaugeMetricModel gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

}
