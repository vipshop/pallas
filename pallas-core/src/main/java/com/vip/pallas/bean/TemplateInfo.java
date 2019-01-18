package com.vip.pallas.bean;

import java.io.Serializable;

public class TemplateInfo implements Serializable {
    private Long id;
    private String templateName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
