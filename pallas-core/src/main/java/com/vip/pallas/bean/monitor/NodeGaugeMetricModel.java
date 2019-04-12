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

/**
 *  gauge metric
 */
public class NodeGaugeMetricModel implements Serializable {

    private String nodeName;
    private String transportAddress;
    private Double jvmHeapUsage;
    /*available disk space: byte*/
    private Long availableFS;
    /*document count*/
    private Long documentCount;
    /*document disk store:byte*/
    private Long documentStore;
    /*index count*/
    private Integer indexCount;
    /*shard count*/
    private Integer shardCount;

    private Long uptime_in_ms;

    private String uptime;

    private String nodeRole;

    private String status;

    private Double processCpuPercent;

    private Double osCpuPercent;

    private Double load_1m;

    private boolean master;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public Long getUptime_in_ms() {
        return uptime_in_ms;
    }

    public void setUptime_in_ms(Long uptime_in_ms) {
        this.uptime_in_ms = uptime_in_ms;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public Double getLoad_1m() {
        return load_1m;
    }

    public void setLoad_1m(Double load_1m) {
        this.load_1m = load_1m;
    }

    public Double getOsCpuPercent() {
        return osCpuPercent;
    }

    public void setOsCpuPercent(Double osCpuPercent) {
        this.osCpuPercent = osCpuPercent;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public void setTransportAddress(String transportAddress) {
        this.transportAddress = transportAddress;
    }

    public Double getProcessCpuPercent() {
        return processCpuPercent;
    }

    public void setProcessCpuPercent(Double processCpuPercent) {
        this.processCpuPercent = processCpuPercent;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Long getAvailableFS() {
        return availableFS;
    }

    public void setAvailableFS(Long availableFS) {
        this.availableFS = availableFS;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Double getJvmHeapUsage() {
        return jvmHeapUsage;
    }

    public void setJvmHeapUsage(Double jvmHeapUsage) {
        this.jvmHeapUsage = jvmHeapUsage;
    }

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public Long getDocumentStore() {
        return documentStore;
    }

    public void setDocumentStore(Long documentStore) {
        this.documentStore = documentStore;
    }

    public Integer getIndexCount() {
        return indexCount;
    }

    public void setIndexCount(Integer indexCount) {
        this.indexCount = indexCount;
    }

    public Integer getShardCount() {
        return shardCount;
    }

    public void setShardCount(Integer shardCount) {
        this.shardCount = shardCount;
    }

    public String getNodeRole() {
        return nodeRole;
    }

    public void setNodeRole(String nodeRole) {
        this.nodeRole = nodeRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
