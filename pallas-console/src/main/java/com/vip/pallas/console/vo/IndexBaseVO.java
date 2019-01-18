package com.vip.pallas.console.vo;

import java.io.Serializable;

public class IndexBaseVO implements Serializable {

    private Long id;
    private String indexName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
