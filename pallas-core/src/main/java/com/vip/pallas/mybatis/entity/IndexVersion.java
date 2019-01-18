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

package com.vip.pallas.mybatis.entity;

import java.util.Date;

public class IndexVersion {
	
    private Long id;
    
    private Long indexId;

    private String versionName;

    private Boolean isUsed;

    private String syncStat;

    private Byte numOfShards;
    
    private Byte numOfReplication;

	private String vdpQueue;

    private String routingKey;
    
    private String idField;

    private String updateTimeField;    

    private Date createTime;

    private Date updateTime;
    
    private Boolean isSync;
    
    private int vdp;
    
    private Boolean filterFields;
    
    private Boolean checkSum;
    
    private String preferExecutor;
    
    private Long clusterId;

    private String allocationNodes;

    private Boolean dynamic;

    private Long indexSlowThreshold;

    private Long fetchSlowThreshold;

    private Long querySlowThreshold;

    private Byte refreshInterval;


    
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

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName == null ? null : versionName.trim();
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public String getSyncStat() {
        return syncStat;
    }

    public void setSyncStat(String syncStat) {
        this.syncStat = syncStat == null ? null : syncStat.trim();
    }

    public Byte getNumOfShards() {
        return numOfShards;
    }

    public void setNumOfShards(Byte numOfShards) {
        this.numOfShards = numOfShards;
    }

    public String getVdpQueue() {
        return vdpQueue;
    }

    public void setVdpQueue(String vdpQueue) {
        this.vdpQueue = vdpQueue == null ? null : vdpQueue.trim();
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey == null ? null : routingKey.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField == null ? null : idField.trim();
    }

    public String getUpdateTimeField() {
        return updateTimeField;
    }

    public void setUpdateTimeField(String updateTimeField) {
        this.updateTimeField = updateTimeField == null ? null : updateTimeField.trim();
    }    
    
    public Byte getNumOfReplication() {
		return numOfReplication;
	}

	public void setNumOfReplication(Byte numOfReplication) {
		this.numOfReplication = numOfReplication;
	}

	public Boolean getIsSync() {
		return isSync;
	}

	public void setIsSync(Boolean isSync) {
		this.isSync = isSync;
	}

	public int getVdp() {
		return vdp;
	}

	public void setVdp(int vdp) {
		this.vdp = vdp;
	}

	public Boolean getFilterFields() {
		return filterFields;
	}

	public void setFilterFields(Boolean filterFields) {
		this.filterFields = filterFields;
	}

	public String getPreferExecutor() {
		return preferExecutor;
	}

	public void setPreferExecutor(String preferExecutor) {
		this.preferExecutor = preferExecutor;
	}

	public Boolean getCheckSum() {
		return checkSum;
	}

	public void setCheckSum(Boolean checkSum) {
		this.checkSum = checkSum;
	}

	public Long getClusterId() {
		return clusterId;
	}

    public Long getIndexSlowThreshold() {
        return indexSlowThreshold;
    }

    public void setIndexSlowThreshold(Long indexSlowThreshold) {
        this.indexSlowThreshold = indexSlowThreshold;
    }

    public Long getFetchSlowThreshold() {
        return fetchSlowThreshold;
    }

    public void setFetchSlowThreshold(Long fetchSlowThreshold) {
        this.fetchSlowThreshold = fetchSlowThreshold;
    }

    public Long getQuerySlowThreshold() {
        return querySlowThreshold;
    }

    public void setQuerySlowThreshold(Long querySlowThreshold) {
        this.querySlowThreshold = querySlowThreshold;
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

    public Boolean getDynamic() {
        return dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }

    public Byte getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Byte refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    @Override
    public String toString() {
        return "IndexVersion{" +
                "id=" + id +
                ", indexId=" + indexId +
                ", versionName='" + versionName + '\'' +
                ", isUsed=" + isUsed +
                ", syncStat='" + syncStat + '\'' +
                ", numOfShards=" + numOfShards +
                ", numOfReplication=" + numOfReplication +
                ", vdpQueue='" + vdpQueue + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", idField='" + idField + '\'' +
                ", updateTimeField='" + updateTimeField + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isSync=" + isSync +
                ", vdp=" + vdp +
                ", filterFields=" + filterFields +
                ", checkSum=" + checkSum +
                ", preferExecutor='" + preferExecutor + '\'' +
                ", clusterId=" + clusterId +
                ", allocationNodes='" + allocationNodes + '\'' +
                ", dynamic=" + dynamic +
                ", indexSlowThreshold=" + indexSlowThreshold +
                ", fetchSlowThreshold=" + fetchSlowThreshold +
                ", querySlowThreshold=" + querySlowThreshold +
                ", refreshInterval=" + refreshInterval +
                '}';
    }
}