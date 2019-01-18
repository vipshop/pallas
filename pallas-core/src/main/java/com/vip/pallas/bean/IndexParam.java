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

import java.util.List;

import com.vip.pallas.mybatis.entity.DataSource;

public class IndexParam {
	
	private Long versionId;
	private String clusterName;
	private String clusterIPList;
	private String indexName;
	private String routingField;
	private String idField;
	private String updateTimeField;
	
	private List<DataSource> dataSourceList;
	
	private boolean multipleDataSource;
	private String arrayFields;
	private Long indexId;
    private int vdp;
    private boolean filterFields;
    private boolean checkSum;
    private String preferExecutor;
	
	public String getArrayFields() {
		return arrayFields;
	}
	public void setArrayFields(String arrayFields) {
		this.arrayFields = arrayFields;
	}
	
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getClusterIPList() {
		return clusterIPList;
	}
	public void setClusterIPList(String clusterIPList) {
		this.clusterIPList = clusterIPList;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public String getRoutingField() {
		return routingField;
	}
	public void setRoutingField(String routingField) {
		this.routingField = routingField;
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
	public Long getVersionId() {
		return versionId;
	}
	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}
	public Long getIndexId() {
		return indexId;
	}
	public void setIndexId(Long indexId) {
		this.indexId = indexId;
	}
	public List<DataSource> getDataSourceList() {
		return dataSourceList;
	}
	public void setDataSourceList(List<DataSource> dataSourceList) {
		this.dataSourceList = dataSourceList;
	}
	public boolean isMultipleDataSource() {
		return getDataSourceList().size() >1 ? true: false;
	}
	public int getVdp() {
		return vdp;
	}
	public void setVdp(int vdp) {
		this.vdp = vdp;
	}
	public boolean isFilterFields() {
		return filterFields;
	}
	public void setFilterFields(boolean filterFields) {
		this.filterFields = filterFields;
	}
	public boolean isCheckSum() {
		return checkSum;
	}
	public void setCheckSum(boolean checkSum) {
		this.checkSum = checkSum;
	}
	public String getPreferExecutor() {
		return preferExecutor;
	}
	public void setPreferExecutor(String preferExecutor) {
		this.preferExecutor = preferExecutor;
	}
	@Override
	public String toString() {
		return "IndexParam [versionId=" + versionId + ", clusterName="
				+ clusterName + ", clusterIPList=" + clusterIPList
				+ ", indexName=" + indexName + ", routingField=" + routingField
				+ ", idField=" + idField + ", updateTimeField="
				+ updateTimeField + ", dataSourceList=" + dataSourceList
				+ ", multipleDataSource=" + multipleDataSource
				+ ", arrayFields=" + arrayFields + ", indexId=" + indexId
				+ ", vdp=" + vdp + ", filterFields=" + filterFields
				+ ", checkSum=" + checkSum + ", preferExecutor="
				+ preferExecutor + "]";
	}
	

}