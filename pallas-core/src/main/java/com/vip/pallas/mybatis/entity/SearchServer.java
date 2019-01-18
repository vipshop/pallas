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

import java.beans.Transient;
import java.io.Serializable;
import java.util.Date;

public class SearchServer implements Serializable {
	
	private boolean healthy = false;
    /**
     * 自增id
     */
    private Long id;

    /**
     * pallas search节点ip与port
     */
    private String ipport;

    /**
     * 所属集群
     */
    private String cluster;

    /**
     * 记录生成时间
     */
    private Date createTime;

    /**
     * 记录更新时间
     */
    private Date updateTime;

    /**
     * 节点上报信息，json格式
     */
    private String info;

    /**
     * 是否承载流量，在Console 端控制
     */
    private Boolean takeTraffic;


    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpport() {
        return ipport;
    }

    public void setIpport(String ipport) {
        this.ipport = ipport;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getTakeTraffic() {
        return takeTraffic;
    }

    public void setTakeTraffic(Boolean takeTraffic) {
        this.takeTraffic = takeTraffic;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", ipport=").append(ipport);
        sb.append(", cluster=").append(cluster);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", info=").append(info);
		sb.append(", healthy=").append(healthy);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append(", takeTraffic=").append(takeTraffic);
        sb.append("]");
        return sb.toString();
    }

    @Transient
	public boolean isHealthy() {
		return healthy;
	}

	public void setHealthy(boolean healthy) {
		this.healthy = healthy;
	}
}