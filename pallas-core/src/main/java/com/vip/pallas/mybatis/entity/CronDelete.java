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

public class CronDelete {
    private Long id;

    private Long versionId;

    private String cron;

    private Integer scrollSize;

	private Boolean isSyn = false;

	private Date updateTime;

    private String dsl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron == null ? null : cron.trim();
    }

    public Integer getScrollSize() {
        return scrollSize;
    }

    public void setScrollSize(Integer scrollSize) {
        this.scrollSize = scrollSize;
    }

    public Boolean getIsSyn() {
        return isSyn;
    }

    public void setIsSyn(Boolean isSyn) {
        this.isSyn = isSyn;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getDsl() {
        return dsl;
    }

    public void setDsl(String dsl) {
        this.dsl = dsl == null ? null : dsl.trim();
    }

	@Override
	public String toString() {
		return "CronDelete [id=" + id + ", versionId=" + versionId + ", cron=" + cron + ", scrollSize=" + scrollSize
				+ ", isSyn=" + isSyn + ", updateTime=" + updateTime + ", dsl=" + dsl + "]";
	}
}