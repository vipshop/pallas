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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotBlank;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.google.common.collect.ImmutableSet;
import com.vip.pallas.utils.JsonUtil;
import com.vip.vjtools.vjkit.collection.SetUtil;

/**
 * Created by owen on 08/01/2018.
 */
public class SearchAuthorization {

    public static final String AUTHORIZATION_CAT_INDEXALL = "IndexAll";

    public static final String AUTHORIZATION_CAT_CLUSTERALL = "ClusterAll";

    public static final String AUTHORIZATION_PRIVILEGE_WRITE = "Write";

    public static final String AUTHORIZATION_PRIVILEGE_READONLY = "ReadOnly";
    
    public static final Set<Pool> DEFAULT_POOLS = ImmutableSet.of(Pool.DEFAULT_POOL);

    private Long id;

    @NotBlank(message = "title不能为空")
    private String title;

    @NotBlank(message = "clientToken不能为空")
    private String clientToken;

    private String authorizationItems;

    private boolean enabled;

    private Date createTime;

    private Date updateTime;

    @JsonIgnore
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<AuthorizationItem> authorizationItemList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClientToken() {
        return clientToken;
    }

    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getAuthorizationItems() {
        return authorizationItems;
    }

    public void setAuthorizationItems(String authorizationItems) {
        this.authorizationItems = authorizationItems;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setAuthorizationItemList(List<AuthorizationItem> authorizationItemList) {
        this.authorizationItemList = authorizationItemList;
    }

    public List<AuthorizationItem> getAuthorizationItemList() {
        if (authorizationItemList == null) {
            try {
                authorizationItemList = fromXContent(this.authorizationItems);
            } catch (Exception e) {//NOSONAR
                authorizationItemList = null;
            }
        }
        return authorizationItemList;
    }

    public static <T> String toXContent(T c) throws Exception {
        if (c == null) {
            throw new IllegalArgumentException("AuthorizationItem can not be null");
        }
        return JsonUtil.toJson(c);
    }
    
    /**
     * [{"name":"default"}]
     */
    public static Set<Pool> fromPoolsContent(String json) throws Exception {
    	if (StringUtils.isNotEmpty(json)) {
    		Set<Pool> poolSet = JsonUtil.readValue(json, new TypeReference<Set<Pool>>(){});
    		poolSet.addAll(DEFAULT_POOLS);
    		return poolSet;
    	}
    	return SetUtil.newHashSet(DEFAULT_POOLS);
    }
    
    /**
     * "[{\"name\":\"default\"}]"
     */
    public static String toPoolsContent(Set<Pool> pools) throws Exception {
    	if (CollectionUtils.isEmpty(pools)) {
    		pools = DEFAULT_POOLS;
    	} else {
    		pools.addAll(DEFAULT_POOLS);
    	}
    	return JsonUtil.toJson(pools);
    }
    
    public static List<AuthorizationItem> fromXContent(String json) throws Exception {
        return JsonUtil.readValue(json, new TypeReference<List<AuthorizationItem>>(){});
    }

    @org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuthorizationItem {

        private Long id;

        private String name;

        private Map<String, List<String>> privileges;

        private List<AuthorizationItem> indexPrivileges;
        
        private Set<Pool> pools;

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
    }

    @org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Pool {
    	public static final String DEFAULT_POOL_LABEL = "default";
    	public static final String DEFAULT_PS_CLUSTER_ALL = "all";
    	public static final String DEFAULT_UNIQUE_KEY = getUniqueKey(DEFAULT_POOL_LABEL, DEFAULT_PS_CLUSTER_ALL);
    	
    	public static final Pool DEFAULT_POOL = new Pool(DEFAULT_POOL_LABEL, DEFAULT_PS_CLUSTER_ALL);
    	public static final Set<String> DEFAULT_POOL_ARR = ImmutableSet.of();
    	
    	
    	private String name;

    	@JsonProperty(access = Access.READ_ONLY)
    	private String aliasName;
    	
    	private String psClusterName;

		public Pool() {
		}

		public Pool(String name, String psClusterName) {
			this.name = name;
			this.psClusterName = psClusterName;
		}

		public Pool(String name, String aliasName, String psClusterName) {
			super();
			this.name = name;
			this.aliasName = aliasName;
			this.psClusterName = psClusterName;
		}

		@JsonIgnore
		@JSONField(serialize = false)
		public String genUniqueKey() {
			return getUniqueKey(this.name, this.psClusterName);
		}
		
		@JsonIgnore
		@JSONField(serialize = false)
		public static String getUniqueKey (String name, String psClusterName) {
			return name + ":" + psClusterName;
		}
		
		/**
	     * [""]
	     */
		public static Set<String> fromPoolsContent(String json) throws Exception {
			if (StringUtils.isNotEmpty(json)) {
				Set<String> poolSet = JsonUtil.readValue(json, new TypeReference<Set<String>>() {
				});
				return poolSet;
			}
			return SetUtil.newHashSet();
		}

		/**
		 * [""]
		 */
		public static String toPoolsConetent(Set<String> pools) throws Exception {
			if (CollectionUtils.isEmpty(pools)) {
				pools = DEFAULT_POOL_ARR;
			}
			return JsonUtil.toJson(pools);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((psClusterName == null) ? 0 : psClusterName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pool other = (Pool) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (psClusterName == null) {
				if (other.psClusterName != null)
					return false;
			} else if (!psClusterName.equals(other.psClusterName))
				return false;
			return true;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getAliasName() {
			return aliasName;
		}

		public void setAliasName(String aliasName) {
			this.aliasName = aliasName;
		}

		public String getPsClusterName() {
			return psClusterName;
		}

		public void setPsClusterName(String psClusterName) {
			this.psClusterName = psClusterName;
		}
	}
}