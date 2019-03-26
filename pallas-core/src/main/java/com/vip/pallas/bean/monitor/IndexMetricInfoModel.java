package com.vip.pallas.bean.monitor;

import java.util.Date;
import java.util.List;

public class IndexMetricInfoModel {
    private IndexGaugeMetricModel gaugeMetric;

    private MonitorMetricModel<Date, Double> index_memory_lucenc_total_in_byte;
    private MonitorMetricModel<Date, Double> index_memory_terms_in_byte;

    private MonitorMetricModel<Date, Double> index_disk_total;
    private MonitorMetricModel<Date, Double> index_disk_primary;

    private MonitorMetricModel<Date, Long> segmentCount;
    private MonitorMetricModel<Date, Long> documentCount;

    private MonitorMetricModel<Date, Double> searchRate ;
    private MonitorMetricModel<Date, Double> indexingRate;

    public IndexGaugeMetricModel getGaugeMetric() {
        return gaugeMetric;
    }

    public void setGaugeMetric(IndexGaugeMetricModel gaugeMetric) {
        this.gaugeMetric = gaugeMetric;
    }

    public MonitorMetricModel<Date, Long> getSegmentCount() {
        return segmentCount;
    }

    public void setSegmentCount(MonitorMetricModel<Date, Long> segmentCount) {
        this.segmentCount = segmentCount;
    }

    public MonitorMetricModel<Date, Long> getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(MonitorMetricModel<Date, Long> documentCount) {
        this.documentCount = documentCount;
    }

    public MonitorMetricModel<Date, Double> getSearchRate() {
        return searchRate;
    }

    public void setSearchRate(MonitorMetricModel<Date, Double> searchRate) {
        this.searchRate = searchRate;
    }

    public MonitorMetricModel<Date, Double> getIndexingRate() {
        return indexingRate;
    }

    public void setIndexingRate(MonitorMetricModel<Date, Double> indexingRate) {
        this.indexingRate = indexingRate;
    }

    public MonitorMetricModel<Date, Double> getIndex_memory_lucenc_total_in_byte() {
        return index_memory_lucenc_total_in_byte;
    }

    public void setIndex_memory_lucenc_total_in_byte(MonitorMetricModel<Date, Double> index_memory_lucenc_total_in_byte) {
        this.index_memory_lucenc_total_in_byte = index_memory_lucenc_total_in_byte;
    }

    public MonitorMetricModel<Date, Double> getIndex_memory_terms_in_byte() {
        return index_memory_terms_in_byte;
    }

    public void setIndex_memory_terms_in_byte(MonitorMetricModel<Date, Double> index_memory_terms_in_byte) {
        this.index_memory_terms_in_byte = index_memory_terms_in_byte;
    }

    public MonitorMetricModel<Date, Double> getIndex_disk_total() {
        return index_disk_total;
    }

    public void setIndex_disk_total(MonitorMetricModel<Date, Double> index_disk_total) {
        this.index_disk_total = index_disk_total;
    }

    public MonitorMetricModel<Date, Double> getIndex_disk_primary() {
        return index_disk_primary;
    }

    public void setIndex_disk_primary(MonitorMetricModel<Date, Double> index_disk_primary) {
        this.index_disk_primary = index_disk_primary;
    }
}
