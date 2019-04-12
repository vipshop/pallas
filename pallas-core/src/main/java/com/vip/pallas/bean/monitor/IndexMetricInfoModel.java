/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.bean.monitor;

import java.io.Serializable;
import java.util.Date;

public class IndexMetricInfoModel extends MetricInfoModel implements Serializable {
    private IndexGaugeMetricModel gaugeMetric;

    private MonitorMetricModel<Date, Double> index_memory_lucenc_total_in_byte;
    private MonitorMetricModel<Date, Double> index_memory_terms_in_byte;

    private MonitorMetricModel<Date, Double> index_disk_total;
    private MonitorMetricModel<Date, Double> index_disk_primary;

    private MonitorMetricModel<Date, Long> segmentCount;
    private MonitorMetricModel<Date, Long> documentCount;

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
