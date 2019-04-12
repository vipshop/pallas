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

public class ClusterGaugeMetricModel implements Serializable {
    private Integer nodeCount;
    private Long indexCount;
    private Long total_memory_byte;
    private Long used_memory_byte;
    private Long totalShardCount;
    private Long unassignedShardCount;
    private Long documentCount;
    private Long document_store_byte;
    private String health;
    private String version;
    private Long max_uptime_in_ms;
    private String max_uptime;

    public String getMax_uptime() {
        return max_uptime;
    }

    public void setMax_uptime(String max_uptime) {
        this.max_uptime = max_uptime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getMax_uptime_in_millis() {
        return max_uptime_in_ms;
    }

    public void setMax_uptime_in_millis(Long max_uptime_in_millis) {
        this.max_uptime_in_ms = max_uptime_in_millis;
    }

    public Integer getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Long getIndexCount() {
        return indexCount;
    }

    public void setIndexCount(Long indexCount) {
        this.indexCount = indexCount;
    }

    public Long getTotal_memory_byte() {
        return total_memory_byte;
    }

    public void setTotal_memory_byte(Long total_memory_byte) {
        this.total_memory_byte = total_memory_byte;
    }

    public Long getUsed_memory_byte() {
        return used_memory_byte;
    }

    public void setUsed_memory_byte(Long used_memory_byte) {
        this.used_memory_byte = used_memory_byte;
    }

    public Long getTotalShardCount() {
        return totalShardCount;
    }

    public void setTotalShardCount(Long totalShardCount) {
        this.totalShardCount = totalShardCount;
    }

    public Long getUnassignedShardCount() {
        return unassignedShardCount;
    }

    public void setUnassignedShardCount(Long unassignedShardCount) {
        this.unassignedShardCount = unassignedShardCount;
    }

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public Long getDocument_store_byte() {
        return document_store_byte;
    }

    public void setDocument_store_byte(Long document_store_byte) {
        this.document_store_byte = document_store_byte;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }
}
