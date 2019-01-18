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

import javax.validation.constraints.NotBlank;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.type.TypeReference;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vip.pallas.utils.JsonUtil;

/**
 * Created by owen on 08/01/2018.
 */
public class SearchAuthorization {

    public static final String AUTHORIZATION_CAT_INDEXALL = "IndexAll";

    public static final String AUTHORIZATION_CAT_CLUSTERALL = "ClusterAll";

    public static final String AUTHORIZATION_PRIVILEGE_WRITE = "Write";

    public static final String AUTHORIZATION_PRIVILEGE_READONLY = "ReadOnly";

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
    }
}