package com.vip.pallas.bean.monitor;

import java.io.Serializable;

public class IndexGaugeMetricModel implements Serializable {
    private String indexName;
    private Long documentCount;
    private String document_store_byte_total;  //primary + replica
    private String document_store_byte_primary;
    private Integer totalShardCount;
    private Integer primaryShardCount;
    private  Integer replicaShardCount;
    private Integer unassignedShardCount;
    private String health;
    private String status;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public String getDocument_store_byte_total() {
        return document_store_byte_total;
    }

    public void setDocument_store_byte_total(String document_store_byte_total) {
        this.document_store_byte_total = document_store_byte_total;
    }

    public String getDocument_store_byte_primary() {
        return document_store_byte_primary;
    }

    public void setDocument_store_byte_primary(String document_store_byte_primary) {
        this.document_store_byte_primary = document_store_byte_primary;
    }

    public Integer getTotalShardCount() {
        return totalShardCount;
    }

    public void setTotalShardCount(Integer totalShardCount) {
        this.totalShardCount = totalShardCount;
    }

    public Integer getPrimaryShardCount() {
        return primaryShardCount;
    }

    public void setPrimaryShardCount(Integer primaryShardCount) {
        this.primaryShardCount = primaryShardCount;
    }

    public Integer getReplicaShardCount() {
        return replicaShardCount;
    }

    public void setReplicaShardCount(Integer replicaShardCount) {
        this.replicaShardCount = replicaShardCount;
    }

    public Integer getUnassignedShardCount() {
        return unassignedShardCount;
    }

    public void setUnassignedShardCount(Integer unassignedShardCount) {
        this.unassignedShardCount = unassignedShardCount;
    }
}
