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
import java.util.List;

public class Index {
	
	private Long id;

	private String indexName;

	private String clusterName;

	private String stat;

	private Date createTime;

	private Date updateTime;

	private String description;

	private List<DataSource> dataSourceList;

	private boolean hasPrivilege = false;

	private boolean hasClusterPrivilege = false;
	
    private String createUser;

    private String clusterDesc;
    private Integer timeout;
    private Integer retry;
	private Integer slowerThan;
	private String httpAddress;
	
	public boolean isHasPrivilege() {
		return hasPrivilege;
	}

	public void setHasPrivilege(boolean hasPrivilege) {
		this.hasPrivilege = hasPrivilege;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName == null ? null : indexName.trim();
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName == null ? null : clusterName.trim();
	}

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat == null ? null : stat.trim();
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description == null ? null : description.trim();
	}

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }

	public String getClusterDesc() {
		return clusterDesc;
	}

	public void setClusterDesc(String clusterDesc) {
		this.clusterDesc = clusterDesc;
	}

	public List<DataSource> getDataSourceList() {
		return dataSourceList;
	}

	public void setDataSourceList(List<DataSource> dataSourceList) {
		this.dataSourceList = dataSourceList;
	}

	public boolean isHasClusterPrivilege() {
		return hasClusterPrivilege;
	}

	public void setHasClusterPrivilege(boolean hasClusterPrivilege) {
		this.hasClusterPrivilege = hasClusterPrivilege;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Integer getRetry() {
		return retry;
	}

	public void setRetry(Integer retry) {
		this.retry = retry;
	}

	public Integer getSlowerThan() {
		return slowerThan;
	}

	public void setSlowerThan(Integer slowerThan) {
		this.slowerThan = slowerThan;
	}

	public String getHttpAddress() {
		return httpAddress;
	}

	public void setHttpAddress(String httpAddress) {
		this.httpAddress = httpAddress;
	}

	@Override
	public String toString() {
		return "Index [id=" + id + ", indexName=" + indexName + ", clusterName=" + clusterName + ", stat=" + stat
				+ ", createTime=" + createTime + ", updateTime=" + updateTime + ", description=" + description
				+ ", dataSourceList=" + dataSourceList + ", hasPrivilege=" + hasPrivilege + ", hasClusterPrivilege="
				+ hasClusterPrivilege + ", createUser=" + createUser + ", clusterDesc=" + clusterDesc + ", timeout="
				+ timeout + ", retry=" + retry + ", slowerThan=" + slowerThan + ", httpAddress=" + httpAddress + "]";
	}
}