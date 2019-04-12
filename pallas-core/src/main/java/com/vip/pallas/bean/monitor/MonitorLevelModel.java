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
