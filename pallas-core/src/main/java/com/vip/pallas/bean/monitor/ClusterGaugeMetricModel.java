package com.vip.pallas.bean.monitor;

import java.io.Serializable;

public class ClusterGaugeMetricModel implements Serializable {
    private Integer nodeCount;
    private Long indexCount;
    private Long total_memory_byte;
    private Long used_memory_byte;
    private Long totalShardCount;
    private Long unassignedShardCount;
    private Long documentCount;
    private Long document_store_byte;
    private String health;
    private String version;
    private Long max_uptime_in_ms;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getMax_uptime_in_millis() {
        return max_uptime_in_ms;
    }

    public void setMax_uptime_in_millis(Long max_uptime_in_millis) {
        this.max_uptime_in_ms = max_uptime_in_millis;
    }

    public Integer getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(Integer nodeCount) {
        this.nodeCount = nodeCount;
    }

    public Long getIndexCount() {
        return indexCount;
    }

    public void setIndexCount(Long indexCount) {
        this.indexCount = indexCount;
    }

    public Long getTotal_memory_byte() {
        return total_memory_byte;
    }

    public void setTotal_memory_byte(Long total_memory_byte) {
        this.total_memory_byte = total_memory_byte;
    }

    public Long getUsed_memory_byte() {
        return used_memory_byte;
    }

    public void setUsed_memory_byte(Long used_memory_byte) {
        this.used_memory_byte = used_memory_byte;
    }

    public Long getTotalShardCount() {
        return totalShardCount;
    }

    public void setTotalShardCount(Long totalShardCount) {
        this.totalShardCount = totalShardCount;
    }

    public Long getUnassignedShardCount() {
        return unassignedShardCount;
    }

    public void setUnassignedShardCount(Long unassignedShardCount) {
        this.unassignedShardCount = unassignedShardCount;
    }

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public Long getDocument_store_byte() {
        return document_store_byte;
    }

    public void setDocument_store_byte(Long document_store_byte) {
        this.document_store_byte = document_store_byte;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }
}
