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

public class IndexGaugeMetricModel implements Serializable {
    private String indexName;
    private Long documentCount;
    private Long document_store_byte_total;  //primary + replica
    private Long document_store_byte_primary;
    private Integer totalShardCount;
    private Integer primaryShardCount;
    private  Integer replicaShardCount;
    private Integer unassignedShardCount;
    private String health;
    private String status;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public Long getDocument_store_byte_total() {
        return document_store_byte_total;
    }

    public void setDocument_store_byte_total(Long document_store_byte_total) {
        this.document_store_byte_total = document_store_byte_total;
    }

    public Long getDocument_store_byte_primary() {
        return document_store_byte_primary;
    }

    public void setDocument_store_byte_primary(Long document_store_byte_primary) {
        this.document_store_byte_primary = document_store_byte_primary;
    }

    public Integer getTotalShardCount() {
        return totalShardCount;
    }

    public void setTotalShardCount(Integer totalShardCount) {
        this.totalShardCount = totalShardCount;
    }

    public Integer getPrimaryShardCount() {
        return primaryShardCount;
    }

    public void setPrimaryShardCount(Integer primaryShardCount) {
        this.primaryShardCount = primaryShardCount;
    }

    public Integer getReplicaShardCount() {
        return replicaShardCount;
    }

    public void setReplicaShardCount(Integer replicaShardCount) {
        this.replicaShardCount = replicaShardCount;
    }

    public Integer getUnassignedShardCount() {
        return unassignedShardCount;
    }

    public void setUnassignedShardCount(Integer unassignedShardCount) {
        this.unassignedShardCount = unassignedShardCount;
    }
}
