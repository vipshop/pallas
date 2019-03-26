package com.vip.pallas.bean.monitor;

import java.io.Serializable;
import java.util.List;

public class MonitorMetricModel<X, Y> implements Serializable {

    private List<MetricModel<X, Y>> metricModel;
    private String unit;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<MetricModel<X, Y>> getMetricModel() {
        return metricModel;
    }

    public void setMetricModel(List<MetricModel<X, Y>> metricModel) {
        this.metricModel = metricModel;
    }

    public static class MetricModel<X, Y> implements Serializable {
        //指标x轴
        private X x;
        //指标y轴
        private Y y;

        public X getX() {
            return x;
        }

        public void setX(X x) {
            this.x = x;
        }

        public Y getY() {
            return y;
        }

        public void setY(Y y) {
            this.y = y;
        }

        public MetricModel(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        public MetricModel() {
            super();
        }
    }

    public MonitorMetricModel(List<MetricModel<X, Y>> metricModel, String unit) {
        this.metricModel = metricModel;
        this.unit = unit;
    }

    public MonitorMetricModel() {
        super();
    }
}
