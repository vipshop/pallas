package com.vip.pallas.search.model;

public class TemplateWithTimeoutRetry {

	private Long templateId;

	private Long indexId;

	private String templateName;
	private String clusterName;

	private String indexName;

    private Integer timeout;
    private Integer retry;
    
	public Long getTemplateId() {
		return templateId;
	}
	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}
	public Long getIndexId() {
		return indexId;
	}
	public void setIndexId(Long indexId) {
		this.indexId = indexId;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public Integer getRetry() {
		return retry;
	}
	public void setRetry(Integer retry) {
		this.retry = retry;
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	@Override
	public String toString() {
		return "TemplateWithTimeoutRetry [templateId=" + templateId + ", indexId=" + indexId + ", templateName="
				+ templateName + ", clusterName=" + clusterName + ", indexName=" + indexName + ", timeout=" + timeout
				+ ", retry=" + retry + "]";
	}


}
