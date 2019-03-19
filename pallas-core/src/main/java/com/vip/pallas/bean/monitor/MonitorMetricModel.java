package com.vip.pallas.bean.monitor;

import java.io.Serializable;

public class MonitorMetricModel<X, Y> implements Serializable {
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

    public MonitorMetricModel(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public MonitorMetricModel() {
        super();
    }
}
