package com.vip.pallas.search.model;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Splitter;
import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.utils.IPUtils;
import com.vip.vjtools.vjkit.collection.SetUtil;

@JsonInclude(value = Include.NON_EMPTY)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
public class SearchServer {
	private static String localIpport = IPUtils.localIp4Str() + ":" + PallasSearchProperties.PALLAS_SEARCH_PORT;
	private static String clusterName = PallasSearchProperties.PALLAS_SEARCH_CLUSTER;
	private static Set<String> poolNames = splitPoolName(PallasSearchProperties.SEARCH_POOL_NAME);
	
	private String cluster;
	
	private String ipport;
	
	private Set<String> pools;
	
	private boolean takeTraffic;
	
	private String info;
	
	public SearchServer(boolean takeTraffic, String info) {
		super();
		this.cluster = clusterName;
		this.ipport = localIpport;
		this.pools = poolNames;
		this.takeTraffic = takeTraffic;
		this.info = info;
	}
	
	public SearchServer(String cluster, String ipport, String pool, boolean takeTraffic, String info) {
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
					omitEmptyStrings().split(poolName).iterator();
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

	public boolean isTakeTraffic() {
		return takeTraffic;
	}

	public void setTakeTraffic(boolean takeTraffic) {
		this.takeTraffic = takeTraffic;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}
