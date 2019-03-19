package com.vip.pallas.bean.monitor;

import java.io.Serializable;

public class IndexGaugeMetricModel implements Serializable {
    private Long documentCount;
    private Long document_store_byte_total;  //primary + replica
    private Long document_store_byte_primary;
    private Long totalShardCount;
    private Long unassignedShardCount;

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public Long getDocument_store_byte_total() {
        return document_store_byte_total;
    }

    public void setDocument_store_byte_total(Long document_store_byte_total) {
        this.document_store_byte_total = document_store_byte_total;
    }

    public Long getDocument_store_byte_primary() {
        return document_store_byte_primary;
    }

    public void setDocument_store_byte_primary(Long document_store_byte_primary) {
        this.document_store_byte_primary = document_store_byte_primary;
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
}
