package com.vip.pallas.bean.monitor;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class NodeMetricInfoModel implements Serializable {

    private NodeGaugeMetricModel gaugeMetric;

    private MonitorMetricModel<Date, Long> gcCountOld;
    private MonitorMetricModel<Date, Long> gcCountYoung;

    private MonitorMetricModel<Date, Long> gc_duration_old_ms;
    private MonitorMetricModel<Date, Long> gc_duration_young_ms;

    private MonitorMetricModel<Date, Double> jvm_heap_max_byte;
    private MonitorMetricModel<Date, Double> jvm_heap_used_byte;

    private MonitorMetricModel<Date, Double> cpuNodePercent;
    private MonitorMetricModel<Date, Double> cpuProcessPerent;

    private MonitorMetricModel<Date, Double> index_memory_lucenc_total_byte;
    private MonitorMetricModel<Date, Double> index_memory_terms_bytes;

    private MonitorMetricModel<Date, Double> systemLoad;

    private MonitorMetricModel<Date, Long> segmentCount;

    private MonitorMetricModel<Date, Double> indexingLatency;
    private MonitorMetricModel<Date, Double> searchLatency;

    private MonitorMetricModel<Date, Integer> searchThreadpoolQueue;
    private MonitorMetricModel<Date, Integer> searchThreadpoolReject;

    private MonitorMetricModel<Date, Integer> indexThreadpoolQueue;
    private MonitorMetricModel<Date, Integer> indexThreadpoolReject;

    private MonitorMetricModel<Date, Integer> bulkThreadpoolQueue;
    private MonitorMetricModel<Date, Integer> bulkThreadpoolReject;


    private MonitorMetricModel<Date, Integer> httpOpenCurrent;

    public NodeGaugeMetricModel getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(NodeGaugeMetricModel gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

    public MonitorMetricModel<Date, Long> getGcCountOld() {
        return gcCountOld;
    }

    public void setGcCountOld(MonitorMetricModel<Date, Long> gcCountOld) {
        this.gcCountOld = gcCountOld;
    }

    public MonitorMetricModel<Date, Long> getGcCountYoung() {
        return gcCountYoung;
    }

    public void setGcCountYoung(MonitorMetricModel<Date, Long> gcCountYoung) {
        this.gcCountYoung = gcCountYoung;
    }

    public MonitorMetricModel<Date, Long> getGc_duration_old_ms() {
        return gc_duration_old_ms;
    }

    public void setGc_duration_old_ms(MonitorMetricModel<Date, Long> gc_duration_old_ms) {
        this.gc_duration_old_ms = gc_duration_old_ms;
    }

    public MonitorMetricModel<Date, Long> getGc_duration_young_ms() {
        return gc_duration_young_ms;
    }

    public void setGc_duration_young_ms(MonitorMetricModel<Date, Long> gc_duration_young_ms) {
        this.gc_duration_young_ms = gc_duration_young_ms;
    }


    public MonitorMetricModel<Date, Double> getCpuNodePercent() {
        return cpuNodePercent;
    }

    public void setCpuNodePercent(MonitorMetricModel<Date, Double> cpuNodePercent) {
        this.cpuNodePercent = cpuNodePercent;
    }

    public MonitorMetricModel<Date, Double> getCpuProcessPerent() {
        return cpuProcessPerent;
    }

    public void setCpuProcessPerent(MonitorMetricModel<Date, Double> cpuProcessPerent) {
        this.cpuProcessPerent = cpuProcessPerent;
    }

    public MonitorMetricModel<Date, Double> getSystemLoad() {
        return systemLoad;
    }

    public void setSystemLoad(MonitorMetricModel<Date, Double> systemLoad) {
        this.systemLoad = systemLoad;
    }

    public MonitorMetricModel<Date, Long> getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount(MonitorMetricModel<Date, Long> segmentCount) {
        this.segmentCount = segmentCount;
    }

    public MonitorMetricModel<Date, Double> getIndexingLatency() {
        return indexingLatency;
    }

    public void setIndexingLatency(MonitorMetricModel<Date, Double> indexingLatency) {
        this.indexingLatency = indexingLatency;
    }

    public MonitorMetricModel<Date, Double> getSearchLatency() {
        return searchLatency;
    }

    public void setSearchLatency(MonitorMetricModel<Date, Double> searchLatency) {
        this.searchLatency = searchLatency;
    }

    public MonitorMetricModel<Date, Integer> getSearchThreadpoolQueue() {
        return searchThreadpoolQueue;
    }

    public void setSearchThreadpoolQueue(MonitorMetricModel<Date, Integer> searchThreadpoolQueue) {
        this.searchThreadpoolQueue = searchThreadpoolQueue;
    }

    public MonitorMetricModel<Date, Integer> getSearchThreadpoolReject() {
        return searchThreadpoolReject;
    }

    public void setSearchThreadpoolReject(MonitorMetricModel<Date, Integer> searchThreadpoolReject) {
        this.searchThreadpoolReject = searchThreadpoolReject;
    }

    public MonitorMetricModel<Date, Integer> getIndexThreadpoolQueue() {
        return indexThreadpoolQueue;
    }

    public void setIndexThreadpoolQueue(MonitorMetricModel<Date, Integer> indexThreadpoolQueue) {
        this.indexThreadpoolQueue = indexThreadpoolQueue;
    }

    public MonitorMetricModel<Date, Integer> getIndexThreadpoolReject() {
        return indexThreadpoolReject;
    }

    public void setIndexThreadpoolReject(MonitorMetricModel<Date, Integer> indexThreadpoolReject) {
        this.indexThreadpoolReject = indexThreadpoolReject;
    }

    public MonitorMetricModel<Date, Integer> getBulkThreadpoolQueue() {
        return bulkThreadpoolQueue;
    }

    public void setBulkThreadpoolQueue(MonitorMetricModel<Date, Integer> bulkThreadpoolQueue) {
        this.bulkThreadpoolQueue = bulkThreadpoolQueue;
    }

    public MonitorMetricModel<Date, Integer> getBulkThreadpoolReject() {
        return bulkThreadpoolReject;
    }

    public void setBulkThreadpoolReject(MonitorMetricModel<Date, Integer> bulkThreadpoolReject) {
        this.bulkThreadpoolReject = bulkThreadpoolReject;
    }

    public MonitorMetricModel<Date, Integer> getHttpOpenCurrent() {
        return httpOpenCurrent;
    }

    public void setHttpOpenCurrent(MonitorMetricModel<Date, Integer> httpOpenCurrent) {
        this.httpOpenCurrent = httpOpenCurrent;
    }

    public MonitorMetricModel<Date, Double> getJvm_heap_max_byte() {
        return jvm_heap_max_byte;
    }

    public void setJvm_heap_max_byte(MonitorMetricModel<Date, Double> jvm_heap_max_byte) {
        this.jvm_heap_max_byte = jvm_heap_max_byte;
    }

    public MonitorMetricModel<Date, Double> getJvm_heap_used_byte() {
        return jvm_heap_used_byte;
    }

    public void setJvm_heap_used_byte(MonitorMetricModel<Date, Double> jvm_heap_used_byte) {
        this.jvm_heap_used_byte = jvm_heap_used_byte;
    }

    public MonitorMetricModel<Date, Double> getIndex_memory_lucenc_total_byte() {
        return index_memory_lucenc_total_byte;
    }

    public void setIndex_memory_lucenc_total_byte(MonitorMetricModel<Date, Double> index_memory_lucenc_total_byte) {
        this.index_memory_lucenc_total_byte = index_memory_lucenc_total_byte;
    }

    public MonitorMetricModel<Date, Double> getIndex_memory_terms_bytes() {
        return index_memory_terms_bytes;
    }

    public void setIndex_memory_terms_bytes(MonitorMetricModel<Date, Double> index_memory_terms_bytes) {
        this.index_memory_terms_bytes = index_memory_terms_bytes;
    }
}
