package com.vip.pallas.console.vo;

public class BatchSubmitVO{
    private Long indexId ;
    private String templateIds;
    private String historyDesc;

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public String getTemplateIds() {
        return templateIds;
    }
    public void setTemplateIds(String templateIds) {
        this.templateIds = templateIds;
    }
    public String getHistoryDesc() {
        return historyDesc;
    }
    public void setHistoryDesc(String historyDesc) {
        this.historyDesc = historyDesc;
    }
}
