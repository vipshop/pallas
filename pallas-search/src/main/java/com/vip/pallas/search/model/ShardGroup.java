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

import java.util.List;

public class ShardGroup {

	private String preferNodes;

	private List<String> serverList;

	private String indexName;

	private String id; // indexName:preferNodes e.g approve2_108387:bKiyb20ST52SSUli01BheQ,tskFefP6QzCeJ14df_Dfhg

	public ShardGroup(String preferNodes, List<String> ipAndPortList, String indexName) {
		this.preferNodes = preferNodes;
		this.serverList = ipAndPortList;
		this.indexName = indexName;
		this.setId(indexName + ":" + preferNodes);
	}

	public String getPreferNodes() {
		return preferNodes;
	}

	public void setPreferNodes(String preferNodes) {
		this.preferNodes = preferNodes;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getServerList() {
		return serverList;
	}

	public void setServerList(List<String> serverList) {
		this.serverList = serverList;
	}
}
