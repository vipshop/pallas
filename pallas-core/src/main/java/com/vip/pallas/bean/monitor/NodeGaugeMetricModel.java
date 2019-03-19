package com.vip.pallas.bean.monitor;

import java.io.Serializable;

/**
 *  gauge metric
 */
public class NodeGaugeMetricModel implements Serializable {

    private String nodeName;
    private Double jvmHeapUsage;
    /*available disk space: byte*/
    private Long availableFS;
    /*document count*/
    private Long documentCount;
    /*document disk store:byte*/
    private Long documentStore;
    /*index count*/
    private Long indexCount;
    /*shard count*/
    private Long shardCount;
    //private String update; // 类型再议
    private String type;
    /**/
    private String status;

    public Long getFsFree() {
        return availableFS;
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

    public Long getIndexCount() {
        return indexCount;
    }

    public void setIndexCount(Long indexCount) {
        this.indexCount = indexCount;
    }

    public Long getShardCount() {
        return shardCount;
    }

    public void setShardCount(Long shardCount) {
        this.shardCount = shardCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
