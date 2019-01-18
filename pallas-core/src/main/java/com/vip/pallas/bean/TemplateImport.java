package com.vip.pallas.bean;


import java.util.List;
import java.io.Serializable;

public class TemplateImport implements Serializable {
    private Long indexId;
    private List<TemplateInfo> templateInfos;

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public List<TemplateInfo> getTemplateInfos() {
        return templateInfos;
    }

    public void setTemplateInfos(List<TemplateInfo> templateInfos) {
        this.templateInfos = templateInfos;
    }
}
