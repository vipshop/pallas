package com.vip.pallas.search.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Cluster {

    private Long id;
    
	private String clusterId;

	private String httpAddress;

	private String clientAddress;

	private String description;

	private boolean hasPrivilege = false;    
	/**
     * 当为逻辑集群时，此项不为空，值为集群id集合，逗号分开，如：3,5
     */
    private String realClusters;
    
	private String accessiblePs;

	//只前端用到此字段
	private boolean logicalCluster = false;

	private Date createTime;
	private Date updateTime;

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

	public boolean isHasPrivilege() {
		return hasPrivilege;
	}

	public void setHasPrivilege(boolean hasPrivilege) {
		this.hasPrivilege = hasPrivilege;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId == null ? null : clusterId.trim();
	}

	public String getHttpAddress() {
		return httpAddress;
	}

	public void setHttpAddress(String httpAddress) {
		this.httpAddress = httpAddress == null ? null : httpAddress.trim();
	}

	public String getClientAddress() {
		return clientAddress;
	}

	public void setClientAddress(String clientAddress) {
		this.clientAddress = clientAddress == null ? null : clientAddress.trim();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description == null ? null : description.trim();
	}

	public String getRealClusters() {
		return realClusters;
	}

	public void setRealClusters(String realClusters) {
		this.realClusters = realClusters;
	}

	@JsonIgnore
	public boolean isLogicalCluster() {
		return StringUtils.isNotBlank(getRealClusters());
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccessiblePs() {
		return accessiblePs;
	}

	public void setAccessiblePs(String accessiblePs) {
		this.accessiblePs = accessiblePs;
	}

	@Override
	public String toString() {
		return "Cluster [id=" + id + ", clusterId=" + clusterId + ", httpAddress=" + httpAddress + ", clientAddress="
				+ clientAddress + ", description=" + description + ", hasPrivilege=" + hasPrivilege + ", realClusters="
				+ realClusters + ", accessPsList=" + accessiblePs + ", logicalCluster=" + isLogicalCluster() + "]";
	}
}