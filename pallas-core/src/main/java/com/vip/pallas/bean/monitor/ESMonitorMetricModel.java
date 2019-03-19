package com.vip.pallas.bean.monitor;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * 采集程序存入到ES中的指标信息
 */
public class ESMonitorMetricModel implements Serializable {
    private Date date;
    private Double value;

    public ESMonitorMetricModel(Date date, Double value) {
        this.date = date;
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
