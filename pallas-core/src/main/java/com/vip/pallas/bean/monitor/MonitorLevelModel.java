package com.vip.pallas.bean.monitor;

import java.io.Serializable;

public class MonitorLevelModel implements Serializable {
    private byte cluster;
    private byte index;

    public byte getCluster() {
        return cluster;
    }

    public void setCluster(byte cluster) {
        this.cluster = cluster;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }


    public static MonitorLevelModel getDefaultModel() {
        MonitorLevelModel  monitorLevelModel = new MonitorLevelModel();
        monitorLevelModel.setCluster((byte)0);
        monitorLevelModel.setIndex((byte)0);
        return monitorLevelModel;
    }
    @Override
    public String toString() {
        return "{\"cluster\": " + cluster + ",\"index\": " + index + "}";
    }
}
