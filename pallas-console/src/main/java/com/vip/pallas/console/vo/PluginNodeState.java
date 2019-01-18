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

package com.vip.pallas.console.vo;

/**
 * Created by owen on 14/9/2017.
 */
public class PluginNodeState {

    private String nodeIp;

    private int state;

    private String pluginVersion;

    private String availableVersions;

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public String getAvailableVersions() {
        return availableVersions;
    }

    @org.codehaus.jackson.annotate.JsonIgnore
    public String[] getAvalibleVersionArray() {
        return availableVersions == null ? new String[0] : availableVersions.split(",");
    }

    public void setAvailableVersions(String availableVersions) {
        this.availableVersions = availableVersions;
    }
}