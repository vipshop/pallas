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

package com.vip.pallas.console.vo;

import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class FlowReplayVO implements Serializable {

    private Long id ;

    @NotNull(message = "indexId不能为空")
    private Long indexId ;

    @NotNull(message = "templateId不能为空")
    private Long targetTemplateId ;

    private Long recordId ;

    private Boolean usedLocalTemplate ;

    @NotNull(message = "连接数不能为空")
    @Range(min = 1, max = 3000, message = "连接数范围是 1 ~ 3000")
    private Integer connectionCount ;

    private String jobName ;

    @NotNull(message = "token不能为空")
    private String token ;

    private Integer qps ;

    private Integer loop ;

    private Integer duration ;

    @NotNull(message = "重放线程数不能为空")
    @Range(min = 1, max = 50, message = "重放线程数范围是 1 ~ 50")
    private Integer threadCount ;

    @NotNull(message = "超时时间范围不能为空")
    @Range(min = 1, max = 120000, message = "超时时间范围是 1 ~ 120000")
    private Integer timeout ;

    private String note;

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

    public Long getTargetTemplateId() {
        return targetTemplateId;
    }

    public void setTargetTemplateId(Long targetTemplateId) {
        this.targetTemplateId = targetTemplateId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Boolean getUsedLocalTemplate() {
        return usedLocalTemplate;
    }

    public void setUsedLocalTemplate(Boolean usedLocalTemplate) {
        this.usedLocalTemplate = usedLocalTemplate;
    }

    public Integer getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(Integer connectionCount) {
        this.connectionCount = connectionCount;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getQps() {
        return qps;
    }

    public void setQps(Integer qps) {
        this.qps = qps;
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

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}