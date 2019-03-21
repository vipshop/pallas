package com.vip.pallas.bean.monitor;

import java.io.Serializable;

/**
 *  gauge metric
 */
public class NodeGaugeMetricModel implements Serializable {

    private String nodeName;
    private String transportAddress;
    private Double jvmHeapUsage;
    /*available disk space: byte*/
    private Long availableFS;
    /*document count*/
    private Long documentCount;
    /*document disk store:byte*/
    private Long documentStore;
    /*index count*/
    private Integer indexCount;
    /*shard count*/
    private Integer shardCount;

    private Long uptime_in_ms;

    private String nodeRole;

    private String status;

    private Double processCpuPercent;

    private Double osCpuPercent;

    private Double load_1m;

    private boolean isMaster;

    public Long getUptime_in_ms() {
        return uptime_in_ms;
    }

    public void setUptime_in_ms(Long uptime_in_ms) {
        this.uptime_in_ms = uptime_in_ms;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public Double getLoad_1m() {
        return load_1m;
    }

    public void setLoad_1m(Double load_1m) {
        this.load_1m = load_1m;
    }

    public Double getOsCpuPercent() {
        return osCpuPercent;
    }

    public void setOsCpuPercent(Double osCpuPercent) {
        this.osCpuPercent = osCpuPercent;
    }

    public String getTransportAddress() {
        return transportAddress;
    }

    public void setTransportAddress(String transportAddress) {
        this.transportAddress = transportAddress;
    }

    public Double getProcessCpuPercent() {
        return processCpuPercent;
    }

    public void setProcessCpuPercent(Double processCpuPercent) {
        this.processCpuPercent = processCpuPercent;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Long getAvailableFS() {
        return availableFS;
    }

    public void setAvailableFS(Long availableFS) {
        this.availableFS = availableFS;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Double getJvmHeapUsage() {
        return jvmHeapUsage;
    }

    public void setJvmHeapUsage(Double jvmHeapUsage) {
        this.jvmHeapUsage = jvmHeapUsage;
    }

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public Long getDocumentStore() {
        return documentStore;
    }

    public void setDocumentStore(Long documentStore) {
        this.documentStore = documentStore;
    }

    public Integer getIndexCount() {
        return indexCount;
    }

    public void setIndexCount(Integer indexCount) {
        this.indexCount = indexCount;
    }

    public Integer getShardCount() {
        return shardCount;
    }

    public void setShardCount(Integer shardCount) {
        this.shardCount = shardCount;
    }

    public String getNodeRole() {
        return nodeRole;
    }

    public void setNodeRole(String nodeRole) {
        this.nodeRole = nodeRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
