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

public class FlowReplay {

    public static final int STATE_INIT = 0;

    public static final int STATE_START = 1;

    public static final int STATE_COMPLETED = 2;

    public static final int STATE_CANCEL = 3;

    public static final int STATE_EXCEPTION = 4;

    private Long id;

    private Long recordId;

    private Integer threadCount;

    private Integer connectionCount;

    private Integer executorCount;

    private Integer timeout;

    private Integer qps;

    private String jobName;

    private Long targetTemplateId;

    private Boolean usedLocalTemplate;

    private String token;

    private Integer loop;

    private Integer duration;

    private Integer state;

    private String note;

    private String createUser;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted = Boolean.FALSE;

    private FlowRecord flowRecord;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Integer getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(Integer connectionCount) {
        this.connectionCount = connectionCount;
    }

    public Integer getExecutorCount() {
        return executorCount;
    }

    public void setExecutorCount(Integer executorCount) {
        this.executorCount = executorCount;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getQps() {
        return qps;
    }

    public void setQps(Integer qps) {
        this.qps = qps;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName == null ? null : jobName.trim();
    }

    public Long getTargetTemplateId() {
        return targetTemplateId;
    }

    public void setTargetTemplateId(Long targetTemplateId) {
        this.targetTemplateId = targetTemplateId;
    }

    public Boolean getUsedLocalTemplate() {
        return usedLocalTemplate;
    }

    public void setUsedLocalTemplate(Boolean usedLocalTemplate) {
        this.usedLocalTemplate = usedLocalTemplate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public Integer getLoop() {
        return loop;
    }

    public void setLoop(Integer loop) {
        this.loop = loop;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
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

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
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

    public FlowRecord getFlowRecord() {
        return flowRecord;
    }

    public void setFlowRecord(FlowRecord flowRecord) {
        this.flowRecord = flowRecord;
    }
}