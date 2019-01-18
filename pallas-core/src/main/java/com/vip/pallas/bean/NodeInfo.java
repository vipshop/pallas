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

package com.vip.pallas.bean;

import java.util.Date;
import java.util.List;

public class NodeInfo {
    private String nodeName;
    private String nodeIp;
    private boolean isOnlyMaster;
    private boolean isHealthy;
    private List<String> indexList;
    private String indices;
    private String nodeState;
    private Date nodeTime;
    private Date lastStartupTime;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public boolean isOnlyMaster() {
        return isOnlyMaster;
    }

    public void setOnlyMaster(boolean onlyMaster) {
        isOnlyMaster = onlyMaster;
    }

    public String getNodeState() {
        return nodeState;
    }

    public void setNodeState(String nodeState) {
        this.nodeState = nodeState;
    }

    public Date getNodeTime() {
        return nodeTime;
    }

    public void setNodeTime(Date nodeTime) {
        this.nodeTime = nodeTime;
    }

    public List<String> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<String> indexList) {
        this.indexList = indexList;
    }

    public String getIndices() {
        return indices;
    }

    public void setIndices(String indices) {
        this.indices = indices;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public void setHealthy(boolean healthy) {
        isHealthy = healthy;
    }

    public Date getLastStartupTime() {
        return lastStartupTime;
    }

    public void setLastStartupTime(Date lastStartupTime) {
        this.lastStartupTime = lastStartupTime;
    }
}