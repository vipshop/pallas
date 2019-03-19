package com.vip.pallas.bean.monitor;


import com.vip.pallas.bean.monitor.MonitorMetricModel;
import com.vip.pallas.bean.monitor.NodeGaugeMetricModel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class NodeMetricInfoModel implements Serializable {

    private NodeGaugeMetricModel gaugeMetric;

    private List<MonitorMetricModel<Date, Long>> gcCountOld;
    private List<MonitorMetricModel<Date, Long>> gcCountYoung;

    private List<MonitorMetricModel<Date, Long>> gc_duration_old_ms;
    private List<MonitorMetricModel<Date, Long>> gc_duration_young_ms;

    private List<MonitorMetricModel<Date, Long>> jvm_heap_max_byte;
    private List<MonitorMetricModel<Date, Long>> jvm_heap_used_byte;

    private List<MonitorMetricModel<Date, Double>> cpuNodePercent;
    private List<MonitorMetricModel<Date, Double>> cpuProcessPerent;

    private List<MonitorMetricModel<Date, Long>> index_memory_lucenc_total_byte;
    private List<MonitorMetricModel<Date, Long>> index_memory_terms_bytes;

    private List<MonitorMetricModel<Date, Double>> systemLoad;

    private List<MonitorMetricModel<Date, Long>> segmentCount;

    private List<MonitorMetricModel<Date, Double>> indexingLatency;
    private List<MonitorMetricModel<Date, Double>> searchLatency;

    //cache: fielddata

    private List<MonitorMetricModel<Date, Integer>> searchThreadpoolQueue;
    private List<MonitorMetricModel<Date, Integer>> searchThreadpoolReject;

    private List<MonitorMetricModel<Date, Integer>> indexThreadpoolQueue;
    private List<MonitorMetricModel<Date, Integer>> indexThreadpoolReject;

    private List<MonitorMetricModel<Date, Integer>> bulkThreadpoolQueue;
    private List<MonitorMetricModel<Date, Integer>> bulkThreadpoolReject;


    private List<MonitorMetricModel<Date, Integer>> httpOpenCurrent;

    public NodeGaugeMetricModel getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(NodeGaugeMetricModel gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

    public List<MonitorMetricModel<Date, Long>> getGcCountOld() {
        return gcCountOld;
    }

    public void setGcCountOld(List<MonitorMetricModel<Date, Long>> gcCountOld) {
        this.gcCountOld = gcCountOld;
    }

    public List<MonitorMetricModel<Date, Long>> getGcCountYoung() {
        return gcCountYoung;
    }

    public void setGcCountYoung(List<MonitorMetricModel<Date, Long>> gcCountYoung) {
        this.gcCountYoung = gcCountYoung;
    }

    public List<MonitorMetricModel<Date, Long>> getGc_duration_old_ms() {
        return gc_duration_old_ms;
    }

    public void setGc_duration_old_ms(List<MonitorMetricModel<Date, Long>> gc_duration_old_ms) {
        this.gc_duration_old_ms = gc_duration_old_ms;
    }

    public List<MonitorMetricModel<Date, Long>> getGc_duration_young_ms() {
        return gc_duration_young_ms;
    }

    public void setGc_duration_young_ms(List<MonitorMetricModel<Date, Long>> gc_duration_young_ms) {
        this.gc_duration_young_ms = gc_duration_young_ms;
    }

    public List<MonitorMetricModel<Date, Long>> getJvm_heap_max_byte() {
        return jvm_heap_max_byte;
    }

    public void setJvm_heap_max_byte(List<MonitorMetricModel<Date, Long>> jvm_heap_max_byte) {
        this.jvm_heap_max_byte = jvm_heap_max_byte;
    }

    public List<MonitorMetricModel<Date, Long>> getJvm_heap_used_byte() {
        return jvm_heap_used_byte;
    }

    public void setJvm_heap_used_byte(List<MonitorMetricModel<Date, Long>> jvm_heap_used_byte) {
        this.jvm_heap_used_byte = jvm_heap_used_byte;
    }

    public List<MonitorMetricModel<Date, Double>> getCpuNodePercent() {
        return cpuNodePercent;
    }

    public void setCpuNodePercent(List<MonitorMetricModel<Date, Double>> cpuNodePercent) {
        this.cpuNodePercent = cpuNodePercent;
    }

    public List<MonitorMetricModel<Date, Double>> getCpuProcessPerent() {
        return cpuProcessPerent;
    }

    public void setCpuProcessPerent(List<MonitorMetricModel<Date, Double>> cpuProcessPerent) {
        this.cpuProcessPerent = cpuProcessPerent;
    }

    public List<MonitorMetricModel<Date, Long>> getIndex_memory_lucenc_total_byte() {
        return index_memory_lucenc_total_byte;
    }

    public void setIndex_memory_lucenc_total_byte(List<MonitorMetricModel<Date, Long>> index_memory_lucenc_total_byte) {
        this.index_memory_lucenc_total_byte = index_memory_lucenc_total_byte;
    }

    public List<MonitorMetricModel<Date, Long>> getIndex_memory_terms_bytes() {
        return index_memory_terms_bytes;
    }

    public void setIndex_memory_terms_bytes(List<MonitorMetricModel<Date, Long>> index_memory_terms_bytes) {
        this.index_memory_terms_bytes = index_memory_terms_bytes;
    }

    public List<MonitorMetricModel<Date, Double>> getSystemLoad() {
        return systemLoad;
    }

    public void setSystemLoad(List<MonitorMetricModel<Date, Double>> systemLoad) {
        this.systemLoad = systemLoad;
    }

    public List<MonitorMetricModel<Date, Long>> getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount(List<MonitorMetricModel<Date, Long>> segmentCount) {
        this.segmentCount = segmentCount;
    }

    public List<MonitorMetricModel<Date, Double>> getIndexingLatency() {
        return indexingLatency;
    }

    public void setIndexingLatency(List<MonitorMetricModel<Date, Double>> indexingLatency) {
        this.indexingLatency = indexingLatency;
    }

    public List<MonitorMetricModel<Date, Double>> getSearchLatency() {
        return searchLatency;
    }

    public void setSearchLatency(List<MonitorMetricModel<Date, Double>> searchLatency) {
        this.searchLatency = searchLatency;
    }


    public List<MonitorMetricModel<Date, Integer>> getHttpOpenCurrent() {
        return httpOpenCurrent;
    }

    public void setHttpOpenCurrent(List<MonitorMetricModel<Date, Integer>> httpOpenCurrent) {
        this.httpOpenCurrent = httpOpenCurrent;
    }

    public List<MonitorMetricModel<Date, Integer>> getSearchThreadpoolQueue() {
        return searchThreadpoolQueue;
    }

    public void setSearchThreadpoolQueue(List<MonitorMetricModel<Date, Integer>> searchThreadpoolQueue) {
        this.searchThreadpoolQueue = searchThreadpoolQueue;
    }

    public List<MonitorMetricModel<Date, Integer>> getSearchThreadpoolReject() {
        return searchThreadpoolReject;
    }

    public void setSearchThreadpoolReject(List<MonitorMetricModel<Date, Integer>> searchThreadpoolReject) {
        this.searchThreadpoolReject = searchThreadpoolReject;
    }

    public List<MonitorMetricModel<Date, Integer>> getIndexThreadpoolQueue() {
        return indexThreadpoolQueue;
    }

    public void setIndexThreadpoolQueue(List<MonitorMetricModel<Date, Integer>> indexThreadpoolQueue) {
        this.indexThreadpoolQueue = indexThreadpoolQueue;
    }

    public List<MonitorMetricModel<Date, Integer>> getIndexThreadpoolReject() {
        return indexThreadpoolReject;
    }

    public void setIndexThreadpoolReject(List<MonitorMetricModel<Date, Integer>> indexThreadpoolReject) {
        this.indexThreadpoolReject = indexThreadpoolReject;
    }

    public List<MonitorMetricModel<Date, Integer>> getBulkThreadpoolQueue() {
        return bulkThreadpoolQueue;
    }

    public void setBulkThreadpoolQueue(List<MonitorMetricModel<Date, Integer>> bulkThreadpoolQueue) {
        this.bulkThreadpoolQueue = bulkThreadpoolQueue;
    }

    public List<MonitorMetricModel<Date, Integer>> getBulkThreadpoolReject() {
        return bulkThreadpoolReject;
    }

    public void setBulkThreadpoolReject(List<MonitorMetricModel<Date, Integer>> bulkThreadpoolReject) {
        this.bulkThreadpoolReject = bulkThreadpoolReject;
    }
}
