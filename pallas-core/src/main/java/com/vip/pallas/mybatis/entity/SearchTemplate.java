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

import java.util.Date;

/**
 * Created by Owen.LI on 4/5/2017.
 */
public class SearchTemplate {

	public final static int TYPE_MACRO = 0;

	public final static int TYPE_TEMPLATE = 1;

	public final static String MACRO_START_FLAG = "##__";

	public final static String MACRO_END_FLAG = "__##";

	private Long id;

	private Long indexId;

	private String templateName;

	private String description;

	private String content;

	//last history template
	private String lastContent;

	private String params;

	private int type;

	private int hisCount;

	private Date updateTime;

	private boolean isNewer;

	private boolean isApproving;

    private Integer timeout;
    private Integer retry;

	public String getLastContent() {
		return lastContent;
	}

	public void setLastContent(String lastContent) {
		this.lastContent = lastContent;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getHisCount() {
		return hisCount;
	}

	public void setHisCount(int hisCount) {
		this.hisCount = hisCount;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isNewer() {
		return isNewer;
	}

	public void setNewer(boolean newer) {
		isNewer = newer;
	}

	public boolean isApproving() {
		return isApproving;
	}

	public void setApproving(boolean approving) {
		isApproving = approving;
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
	
	@Override
	public String toString() {
		return "SearchTemplate [id=" + id + ", indexId=" + indexId + ", templateName=" + templateName + ", description="
				+ description + ", content=" + content + ", params=" + params + ", type=" + type + ", hisCount="
				+ hisCount + ", updateTime=" + updateTime + ", isNewer=" + isNewer + ", isApproving=" + isApproving
				+ ", timeout=" + timeout + ", retry=" + retry + "]";
	}
	
}