package com.vip.pallas.bean.monitor;

import java.util.Date;
import java.util.List;

public class IndexMetricInfoModel {
    private IndexGaugeMetricModel gaugeMetric;

    private List<MonitorMetricModel<Date, Long>> index_memory_lucenc_total_in_byte;
    private List<MonitorMetricModel<Date, Long>> index_memory_terms_in_byte;

    private List<MonitorMetricModel<Date, Long>> index_disk_total;
    private List<MonitorMetricModel<Date, Long>> index_disk_primary;

    private List<MonitorMetricModel<Date, Long>> segmentCount;
    private List<MonitorMetricModel<Date, Long>> documentCount;

    private List<MonitorMetricModel<Date, Double>> searchRate ;
    private List<MonitorMetricModel<Date, Double>> indexingRate;

    public IndexGaugeMetricModel getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(IndexGaugeMetricModel gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

    public List<MonitorMetricModel<Date, Long>> getIndex_memory_lucenc_total_in_byte() {
        return index_memory_lucenc_total_in_byte;
    }

    public void setIndex_memory_lucenc_total_in_byte(List<MonitorMetricModel<Date, Long>> index_memory_lucenc_total_in_byte) {
        this.index_memory_lucenc_total_in_byte = index_memory_lucenc_total_in_byte;
    }

    public List<MonitorMetricModel<Date, Long>> getIndex_memory_terms_in_byte() {
        return index_memory_terms_in_byte;
    }

    public void setIndex_memory_terms_in_byte(List<MonitorMetricModel<Date, Long>> index_memory_terms_in_byte) {
        this.index_memory_terms_in_byte = index_memory_terms_in_byte;
    }

    public List<MonitorMetricModel<Date, Long>> getIndex_disk_total() {
        return index_disk_total;
    }

    public void setIndex_disk_total(List<MonitorMetricModel<Date, Long>> index_disk_total) {
        this.index_disk_total = index_disk_total;
    }

    public List<MonitorMetricModel<Date, Long>> getIndex_disk_primary() {
        return index_disk_primary;
    }

    public void setIndex_disk_primary(List<MonitorMetricModel<Date, Long>> index_disk_primary) {
        this.index_disk_primary = index_disk_primary;
    }

    public List<MonitorMetricModel<Date, Long>> getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount(List<MonitorMetricModel<Date, Long>> segmentCount) {
        this.segmentCount = segmentCount;
    }

    public List<MonitorMetricModel<Date, Long>> getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(List<MonitorMetricModel<Date, Long>> documentCount) {
        this.documentCount = documentCount;
    }

    public List<MonitorMetricModel<Date, Double>> getSearchRate() {
        return searchRate;
    }

    public void setSearchRate(List<MonitorMetricModel<Date, Double>> searchRate) {
        this.searchRate = searchRate;
    }

    public List<MonitorMetricModel<Date, Double>> getIndexingRate() {
        return indexingRate;
    }

    public void setIndexingRate(List<MonitorMetricModel<Date, Double>> indexingRate) {
        this.indexingRate = indexingRate;
    }
}
