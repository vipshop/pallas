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

package com.vip.pallas.search.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author dylan
 *
 */
public class ServiceInfo {
	
	private String backendAddress;
	private String targetGroupTitle;
	private String indexName;
	private String clusterName;

	public String getBackendAddress() {
		return backendAddress;
	}

	public void setBackendAddress(String backendAddress) {
		this.backendAddress = backendAddress;
	}

	public ServiceInfo(String backendAddress, String indexName, String clusterName, String targetGroupTitle){
		this.backendAddress = backendAddress;
		this.indexName = indexName;
		this.clusterName = clusterName;
		this.targetGroupTitle = targetGroupTitle == null ? "" : targetGroupTitle;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getTargetGroupTitle() {
		return targetGroupTitle;
	}
}
