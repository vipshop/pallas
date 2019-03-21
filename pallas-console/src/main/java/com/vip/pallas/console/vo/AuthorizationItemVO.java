package com.vip.pallas.console.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vip.pallas.mybatis.entity.SearchAuthorization.AuthorizationItem;
import com.vip.pallas.mybatis.entity.SearchAuthorization.Pool;
import com.vip.vjtools.vjkit.collection.MapUtil;

@org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationItemVO {

	private Long id;

	private String name;

	private Map<String, List<String>> privileges;

	private List<AuthorizationItem> indexPrivileges;

	private Set<Pool> pools;
	
	private Set<Pool> serverPools;

	public AuthorizationItemVO() {
	}
	
	public AuthorizationItemVO(Long id, String name) {
		this.id = id;
		this.name = name;
		this.privileges = new HashMap<>();
		this.indexPrivileges = new ArrayList<>();
		this.pools = new HashSet<>();
	}

	public AuthorizationItemVO(AuthorizationItem item) {
		this.id = item.getId();
		this.name = item.getName();
		this.privileges = MapUtil.isNotEmpty(item.getPrivileges()) ? item.getPrivileges() : new HashMap<>();
		this.indexPrivileges = CollectionUtils.isNotEmpty(item.getIndexPrivileges()) ? item.getIndexPrivileges() : new ArrayList<>();
		this.pools = CollectionUtils.isNotEmpty(item.getPools()) ? item.getPools() : new HashSet<>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, List<String>> getPrivileges() {
		return privileges;
	}

	public void setPrivileges(Map<String, List<String>> privileges) {
		this.privileges = privileges;
	}

	public List<AuthorizationItem> getIndexPrivileges() {
		return indexPrivileges;
	}

	public void setIndexPrivileges(List<AuthorizationItem> indexPrivileges) {
		this.indexPrivileges = indexPrivileges;
	}

	public Set<Pool> getPools() {
		return pools;
	}

	public void setPools(Set<Pool> pools) {
		this.pools = pools;
	}

	public Set<Pool> getServerPools() {
		return serverPools;
	}

	public void setServerPools(Set<Pool> serverPools) {
		this.serverPools = serverPools;
	}
}
