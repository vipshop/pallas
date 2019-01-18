package com.vip.pallas.search.model;

import com.vip.pallas.utils.PallasBasicProperties;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {
    private Long id;

    private String clusterName;

    private String nodeName;

    private String nodeIp;

    private Byte state;

    private Date stateTime;

    private Date lastStartupTime;

    private String note;

    private Date createTime;

    private Date updateTime;

    private Boolean isDeleted;

    public boolean isHealthy(){
        return System.currentTimeMillis() - getUpdateTime().getTime() < 4000 * PallasBasicProperties.PALLAS_ES_HEARTBEAT_INTERVAL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName == null ? null : clusterName.trim();
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName == null ? null : nodeName.trim();
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp == null ? null : nodeIp.trim();
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    public Date getStateTime() {
        return stateTime;
    }

    public void setStateTime(Date stateTime) {
        this.stateTime = stateTime;
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

    public Date getLastStartupTime() {
        return lastStartupTime;
    }

    public void setLastStartupTime(Date lastStartupTime) {
        this.lastStartupTime = lastStartupTime;
    }
}