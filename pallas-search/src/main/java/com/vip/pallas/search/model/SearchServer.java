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

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Splitter;
import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.utils.IPUtils;
import com.vip.vjtools.vjkit.collection.SetUtil;


@org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchServer {
	private static String localIpport = IPUtils.localIp4Str() + ":" + PallasSearchProperties.PALLAS_SEARCH_PORT;
	private static String clusterName = PallasSearchProperties.PALLAS_SEARCH_CLUSTER;
	private static Set<String> poolNames = splitPoolName(PallasSearchProperties.SEARCH_POOL_NAME);
	
	private String cluster;
	
	private String ipport;
	
	private Set<String> pools;
	
	private Boolean takeTraffic;
	
	private Object info;
	
	public SearchServer(Boolean takeTraffic, Object info) {
		super();
		this.cluster = clusterName;
		this.ipport = localIpport;
		this.pools = poolNames;
		this.takeTraffic = takeTraffic;
		this.info = info;
	}
	
	public SearchServer(String cluster, String ipport, String pool, Boolean takeTraffic, Object info) {
		super();
		this.cluster = cluster;
		this.ipport = ipport;
		this.pools = splitPoolName(pool);
		this.takeTraffic = takeTraffic;
		this.info = info;
	}

	private static Set<String> splitPoolName(String poolName) {
		Set<String> poolSets = SetUtil.newHashSet();
		if (StringUtils.isNotEmpty(poolName)) {
			Iterator<String> iterator = Splitter.on(",").trimResults().
					omitEmptyStrings().split(poolName.toLowerCase()).iterator();
			while (iterator.hasNext()) {
				poolSets.add(iterator.next());
			}
		}
		return poolSets;
	}

	public String getCluster() {
		return cluster;
	}

	public void setCluster(String cluster) {
		this.cluster = cluster;
	}

	public String getIpport() {
		return ipport;
	}

	public void setIpport(String ipport) {
		this.ipport = ipport;
	}

	public Set<String> getPools() {
		return pools;
	}

	public void setPools(Set<String> pools) {
		this.pools = pools;
	}

	public Boolean getTakeTraffic() {
		return takeTraffic;
	}

	public void setTakeTraffic(Boolean takeTraffic) {
		this.takeTraffic = takeTraffic;
	}

	public Object getInfo() {
		return info;
	}

	public void setInfo(Object info) {
		this.info = info;
	}
}
