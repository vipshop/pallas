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
