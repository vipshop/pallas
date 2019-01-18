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

public class FlowRecord {
    private Long id;

    private Long indexId;

    private Long templateId;

    private Double sampleRate;

    private Long limit;

    private Date startTime;

    private Date endTime;

    private Long configId;

    private Long total;

    private Integer state;

    private String note;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;

    private FlowRecordConfig flowRecordConfig;

    private int percentage;

    private boolean hasPrivilege = false;

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

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Double sampleRate) {
        this.sampleRate = sampleRate;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note == null ? null : note.trim();
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public FlowRecordConfig getFlowRecordConfig() {
        return flowRecordConfig;
    }

    public void setFlowRecordConfig(FlowRecordConfig flowRecordConfig) {
        this.flowRecordConfig = flowRecordConfig;
    }

    public boolean isHasPrivilege() {
        return hasPrivilege;
    }

    public void setHasPrivilege(boolean hasPrivilege) {
        this.hasPrivilege = hasPrivilege;
    }

    public int getPercentage() {
        if (this.getFlowRecordConfig() != null && this.getFlowRecordConfig().getLimit() != 0) {
            return Math.min(Math.round(this.getTotal() * 100f / this.getFlowRecordConfig().getLimit()), 100);
        } else {
            return 0;
        }
    }
}