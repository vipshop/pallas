package com.vip.pallas.search.model;

import java.util.Date;

public class FlowRecordConfig {
    private Long id;

    private Long indexId;

    private Long templateId;

    private Double sampleRate;

    private Long limit;

    private Date startTime;

    private Date endTime;

    private Boolean isEnable;

    private String note;

    private String createUser;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;

    private Cluster cluster;

    private Index index;

    private SearchTemplate template;

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

    public Boolean getIsEnable() {
        return isEnable;
    }

    public void setIsEnable(Boolean isEnable) {
        this.isEnable = isEnable;
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

    public Index getIndex() {
        return index;
    }
    public void setIndex(Index index) {
        this.index = index;
    }

    public SearchTemplate getTemplate() {
        return template;
    }

    public void setTemplate(SearchTemplate template) {
        this.template = template;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public boolean isHasPrivilege() {
        return hasPrivilege;
    }

    public void setHasPrivilege(boolean hasPrivilege) {
        this.hasPrivilege = hasPrivilege;
    }
}