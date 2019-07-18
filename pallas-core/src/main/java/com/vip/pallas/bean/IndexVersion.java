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

import java.util.ArrayList;
import java.util.List;

public class IndexVersion {
    private Long id;
    private Long indexId;
    private int shardNum;
    private int replicationNum;
	private String vdpQueue;
    private String routingKey;
    private String idField;
    private String updateTimeField;   
    private List<VersionField> schema = new ArrayList<>();
    private int vdp;
    private Boolean filterFields;
    private Boolean checkSum;
    private String preferExecutor;
    private String allocationNodes;
    private Boolean dynamic;
    private Boolean isSync;

	private Long indexSlowThreshold;
	private Long fetchSlowThreshold;
	private Long querySlowThreshold;

	private Byte refreshInterval;
	private String realClusterIds;

	private Long maxResultWindow;

	private Integer totalShardsPerNode;

	private String flushThresholdSize;

	private String syncInterval;

	private String translogDurability;

	private Boolean sourceDisabled;

	private String sourceIncludes;

	private String sourceExcludes;

	public Boolean getSourceDisabled() {
		return sourceDisabled;
	}

	public void setSourceDisabled(Boolean sourceDisabled) {
		this.sourceDisabled = sourceDisabled;
	}

	public String getSourceIncludes() {
		return sourceIncludes;
	}

	public void setSourceIncludes(String sourceIncludes) {
		this.sourceIncludes = sourceIncludes;
	}

	public String getSourceExcludes() {
		return sourceExcludes;
	}

	public void setSourceExcludes(String sourceExcludes) {
		this.sourceExcludes = sourceExcludes;
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

	public void setTotalShardsPerNode(Integer totalShardsPerNode) {
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

    public void addField(VersionField field){
    	getSchema().add(field);
    }
	
	public static class VersionField{
		private String fieldName;
		private String fieldType;
		private String dbFieldType;
		private boolean multi;
		private boolean search;
		private boolean docValue;
		private boolean dynamic;
		private boolean store;
		private List<VersionField> children;
		private List<VersionField> multiField;
		private List<String> copyTo;

		public void addField(VersionField field){
			if(this.children == null){
				setChildren(new ArrayList<>());
			}
			getChildren().add(field);
		}

		public void addMultiField(VersionField field){
			if(this.multiField == null){
				setMultiField(new ArrayList<>());
			}
			getMultiField().add(field);
		}

		public void addCopyToField(String field){
			if(this.copyTo == null){
				setCopyTo(new ArrayList<>());
			}
			getCopyTo().add(field);
		}

		public boolean isStore() {
			return store;
		}

		public void setStore(boolean store) {
			this.store = store;
		}

		public String getFieldName() {
			return fieldName;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		public String getFieldType() {
			return fieldType;
		}
		public void setFieldType(String fieldType) {
			this.fieldType = fieldType;
		}
		public boolean isSearch() {
			return search;
		}
		public void setSearch(boolean search) {
			this.search = search;
		}
		public boolean isDocValue() {
			return docValue;
		}
		public void setDocValue(boolean docValue) {
			this.docValue = docValue;
		}
		public List<VersionField> getChildren() {
			return children;
		}
		public void setChildren(List<VersionField> children) {
			this.children = children;
		}

		public boolean isMulti() {
			return multi;
		}

		public void setMulti(boolean multi) {
			this.multi = multi;
		}

		public String getDbFieldType() {
			return dbFieldType;
		}

		public void setDbFieldType(String dbFieldType) {
			this.dbFieldType = dbFieldType;
		}

		public boolean isDynamic() {
			return dynamic;
		}

		public void setDynamic(boolean dynamic) {
			this.dynamic = dynamic;
		}

		public List<VersionField> getMultiField() {
			return multiField;
		}

		public void setMultiField(List<VersionField> multiField) {
			this.multiField = multiField;
		}

		public List<String> getCopyTo() {
			return copyTo;
		}

		public void setCopyTo(List<String> copyTo) {
			this.copyTo = copyTo;
		}

		@Override
		public String toString() {
			return "VersionField{" +
					"fieldName='" + fieldName + '\'' +
					", dbFieldType='" + dbFieldType + '\'' +
					'}';
		}
	}

	public String getRealClusterIds() {
		return realClusterIds;
	}

	public void setRealClusterIds(String realClusterIds) {
		this.realClusterIds = realClusterIds;
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

	public String getVdpQueue() {
		return vdpQueue;
	}

	public void setVdpQueue(String vdpQueue) {
		this.vdpQueue = vdpQueue;
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}

	public String getIdField() {
		return idField;
	}

	public void setIdField(String idField) {
		this.idField = idField;
	}

	public String getUpdateTimeField() {
		return updateTimeField;
	}

	public void setUpdateTimeField(String updateTimeField) {
		this.updateTimeField = updateTimeField;
	}

	public int getShardNum() {
		return shardNum;
	}

	public void setShardNum(int shardNum) {
		this.shardNum = shardNum;
	}

	public int getReplicationNum() {
		return replicationNum;
	}

	public void setReplicationNum(int replicationNum) {
		this.replicationNum = replicationNum;
	}

	public List<VersionField> getSchema() {
		return schema;
	}

	public void setSchema(List<VersionField> schema) {
		this.schema = schema;
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

	public Boolean getSync() {
		return isSync;
	}

	public void setSync(Boolean sync) {
		isSync = sync;
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

	public Byte getRefreshInterval() {
		return refreshInterval;
	}

	public void setRefreshInterval(Byte refreshInterval) {
		this.refreshInterval = refreshInterval;
	}


}