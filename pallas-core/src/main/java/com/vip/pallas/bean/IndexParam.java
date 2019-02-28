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

import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.DataSource;

public class IndexParam {
	
	private Long versionId;
	private String indexName;
	private String routingField = "id";
	private String idField = "id";
	private String updateTimeField;
	
	private List<DataSource> dataSourceList;
	private List<Cluster> realClusters;
	private boolean multipleDataSource;
	private String arrayFields;
	private String esObjectFields;
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

	public String getEsObjectFields() {
		return esObjectFields;
	}

	public void setEsObjectFields(String esObjectFields) {
		this.esObjectFields = esObjectFields;
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

	public List<Cluster> getRealClusters() {
		return realClusters;
	}

	public void setRealClusters(List<Cluster> realClusters) {
		this.realClusters = realClusters;
	}

	@Override
	public String toString() {
		return "IndexParam [versionId=" + versionId + ", indexName=" + indexName + ", routingField=" + routingField
				+ ", idField=" + idField + ", updateTimeField=" + updateTimeField + ", dataSourceList=" + dataSourceList
				+ ", realClusters=" + realClusters + ", multipleDataSource=" + multipleDataSource + ", arrayFields="
				+ arrayFields + ", esObjectFields=" + esObjectFields + ", indexId=" + indexId + ", vdp=" + vdp
				+ ", filterFields=" + filterFields + ", checkSum=" + checkSum + ", preferExecutor=" + preferExecutor
				+ "]";
	}
}