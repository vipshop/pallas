/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.mybatis.entity;

public class TemplateWithThrottling {

	private Long templateId;
	private Long indexId;
	private String templateName;
	private String clusterName;
	private String indexName;

	private Integer threshold;
	private Integer maxBurstSecs;

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
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	public Integer getMaxBurstSecs() {
		return maxBurstSecs;
	}

	public void setMaxBurstSecs(Integer maxBurstSecs) {
		this.maxBurstSecs = maxBurstSecs;
	}

	@Override
	public String toString() {
		return "TemplateWithThrottling{" + "templateId=" + templateId + ", indexId=" + indexId + ", templateName='"
				+ templateName + '\'' + ", clusterName='" + clusterName + '\'' + ", indexName='" + indexName + '\''
				+ ", threshold=" + threshold + ", maxBurstSecs=" + maxBurstSecs + '}';
	}
}