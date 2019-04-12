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

/**
 * cluser node index common metrics
 */
public class MetricInfoModel implements Serializable {
    private MonitorMetricModel<Date, Double> searchRate;
    private MonitorMetricModel<Date, Double> searchTime;
    private MonitorMetricModel<Date, Double> indexingTime;
    private MonitorMetricModel<Date, Double> indexingRate;
    private MonitorMetricModel<Date, Double> searchLatency;
    private MonitorMetricModel<Date, Double> indexingLatency;

    public MonitorMetricModel<Date, Double> getSearchRate() {
        return searchRate;
    }

    public void setSearchRate(MonitorMetricModel<Date, Double> searchRate) {
        this.searchRate = searchRate;
    }

    public MonitorMetricModel<Date, Double> getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(MonitorMetricModel<Date, Double> searchTime) {
        this.searchTime = searchTime;
    }

    public MonitorMetricModel<Date, Double> getIndexingTime() {
        return indexingTime;
    }

    public void setIndexingTime(MonitorMetricModel<Date, Double> indexingTime) {
        this.indexingTime = indexingTime;
    }

    public MonitorMetricModel<Date, Double> getIndexingRate() {
        return indexingRate;
    }

    public void setIndexingRate(MonitorMetricModel<Date, Double> indexingRate) {
        this.indexingRate = indexingRate;
    }

    public MonitorMetricModel<Date, Double> getSearchLatency() {
        return searchLatency;
    }

    public void setSearchLatency(MonitorMetricModel<Date, Double> searchLatency) {
        this.searchLatency = searchLatency;
    }

    public MonitorMetricModel<Date, Double> getIndexingLatency() {
        return indexingLatency;
    }

    public void setIndexingLatency(MonitorMetricModel<Date, Double> indexingLatency) {
        this.indexingLatency = indexingLatency;
    }
}
