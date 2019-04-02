package com.vip.pallas.search.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexRampup {
    private Long indexId;
    private Long versionId;
    private String clusterName;
    private String fullIndexName;
    private Date beginTime;
    private Date endTime;
    private String state;
    private Long rampupTarget;
    private Long rampupNow;

    public final static String STATE_FINISH = "finish";
    public final static String STATE_STOP = "stop";
    public final static String STATE_DOING = "doing";

    public boolean needRampup(){
        return STATE_DOING.equals(state);
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getRampupTarget() {
        return rampupTarget;
    }

    public void setRampupTarget(long rampupTarget) {
        this.rampupTarget = rampupTarget;
    }

    public long getRampupNow() {
        return rampupNow;
    }

    public void setRampupNow(long rampupNow) {
        this.rampupNow = rampupNow;
    }

    public String getFullIndexName() {
        return fullIndexName;
    }

    public void setFullIndexName(String fullIndexName) {
        this.fullIndexName = fullIndexName;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(long versionId) {
        this.versionId = versionId;
    }

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}