package com.vip.pallas.bean.monitor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


public class MonitorQueryModel implements Serializable {

    @NotBlank(message = "clusterName不能为空")
    private String clusterName;
    private String nodeName;
    private String indexName;
    @NotNull(message = "查询起止时间不能为空")
    private Long from;
    @NotNull(message = "查询起止时间不能为空")
    private Long to;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
