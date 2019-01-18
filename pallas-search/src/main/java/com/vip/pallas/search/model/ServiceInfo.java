package com.vip.pallas.search.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author dylan
 *
 */
public class ServiceInfo {
	
	private String backendAddress;
	private String targetGroupTitle;
	private String indexName;
	private String clusterName;

	public String getBackendAddress() {
		return backendAddress;
	}

	public void setBackendAddress(String backendAddress) {
		this.backendAddress = backendAddress;
	}

	public ServiceInfo(String backendAddress, String indexName, String clusterName, String targetGroupTitle){
		this.backendAddress = backendAddress;
		this.indexName = indexName;
		this.clusterName = clusterName;
		this.targetGroupTitle = targetGroupTitle == null ? "" : targetGroupTitle;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getTargetGroupTitle() {
		return targetGroupTitle;
	}
}
