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


import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

public class IndexVersionDynamicVO implements Serializable {
    private Long id;

    @NotNull(message = "indexId不能为空")
    private Long indexId;

    private Byte numOfReplication;

    private Boolean checkSum;

    private String preferExecutor;

    private Long clusterId;

    private String allocationNodes;

    private Long indexSlowThreshold;

    private Long fetchSlowThreshold;

    private Long querySlowThreshold;

    private Byte refreshInterval;

    private Long maxResultWindow;

    private int totalShardsPerNode;

    private String flushThresholdSize;

    private String syncInterval;

    private String translogDurability;

    private Object schema;

    @NotNull(message = "schema不能为null")
    public Object getSchema() {
        return schema;
    }

    public void setSchema(Object schema) {
        this.schema = schema;
    }

    public Long getMaxResultWindow() {
        return maxResultWindow;
    }

    public void setMaxResultWindow(Long maxResultWindow) {
        this.maxResultWindow = maxResultWindow;
    }

    public int getTotalShardsPerNode() {
        return totalShardsPerNode;
    }

    public void setTotalShardsPerNode(int totalShardsPerNode) {
        this.totalShardsPerNode = totalShardsPerNode;
    }


    public String getFlushThresholdSize() {
        return flushThresholdSize;
    }

    public void setFlushThresholdSize(String flushThresholdSize) {
        this.flushThresholdSize = flushThresholdSize;
    }

    public String getSyncInterval() {
        return syncInterval;
    }

    public void setSyncInterval(String syncInterval) {
        this.syncInterval = syncInterval;
    }

    public String getTranslogDurability() {
        return translogDurability;
    }

    public void setTranslogDurability(String translogDurability) {
        this.translogDurability = translogDurability;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public Byte getNumOfReplication() {
        return numOfReplication;
    }

    public void setNumOfReplication(Byte numOfReplication) {
        this.numOfReplication = numOfReplication;
    }

    public Boolean getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(Boolean checkSum) {
        this.checkSum = checkSum;
    }

    public String getPreferExecutor() {
        return preferExecutor;
    }

    public void setPreferExecutor(String preferExecutor) {
        this.preferExecutor = preferExecutor;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getAllocationNodes() {
        return allocationNodes;
    }

    public void setAllocationNodes(String allocationNodes) {
        this.allocationNodes = allocationNodes;
    }

    public Long getIndexSlowThreshold() {
        return indexSlowThreshold;
    }

    public void setIndexSlowThreshold(Long indexSlowThreshold) {
        if(indexSlowThreshold == 0){
            indexSlowThreshold = -1L;
        }
        this.indexSlowThreshold = indexSlowThreshold;
    }

    public Long getFetchSlowThreshold() {
        return fetchSlowThreshold;
    }

    public void setFetchSlowThreshold(Long fetchSlowThreshold) {
        if(fetchSlowThreshold == 0){
            fetchSlowThreshold = -1L;
        }
        this.fetchSlowThreshold = fetchSlowThreshold;
    }

    public Long getQuerySlowThreshold() {
        return querySlowThreshold;
    }

    public void setQuerySlowThreshold(Long querySlowThreshold) {
        if(querySlowThreshold == 0){
            querySlowThreshold = -1L;
        }
        this.querySlowThreshold = querySlowThreshold;
    }

    public Byte getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Byte refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
}